import java.io.*
import java.net.Socket

private const val ACCEPT = "accept"
private const val DECLINE = "decline"

private const val IN_SIGN_IN = "SIGN_IN"
private const val IN_EXIT = "EXIT"
private const val IN_REQUEST_TO_PLAY = "REQUEST_TO_PLAY"
private const val IN_RESPONSE_ON_REQUEST_TO_PLAY = "RESPONSE_ON_REQUEST_TO_PLAY"
private const val IN_READY_TO_PLAY = "READY_TO_PLAY"
private const val IN_GET_CLIENTS_LIST = "GET_CLIENTS_LIST"

private const val OUT_SEND_LOGIN_RESULT = "SEND_LOGIN_RESULT"
private const val OUT_SEND_LIST_OF_CLIENT = "SEND_LIST_OF_CLIENT"
private const val OUT_SEND_CLIENT_CONNECTED = "SEND_CLIENT_CONNECTED"
private const val OUT_SEND_CLIENT_REMOVED = "SEND_CLIENT_REMOVED"
private const val OUT_REQUEST_TO_PLAY = "REQUEST_TO_PLAY"
private const val OUT_CONNECT_PLAYER_TO_GAME = "CONNECT_PLAYER_TO_GAME"
private const val OUT_DECLINE_REQUEST_TO_PLAY = "DECLINE_REQUEST_TO_PLAY"
private const val OUT_YOUR_TURN = "YOUR_TURN"

class ClientWorker(socket: Socket, var clientName: String, val serverInteraction: ServerInteraction) : Thread() {
    private val readerStream: BufferedReader
    private val writerStream: PrintWriter

    private var isRunning = true
    private var opponent = ""

    init {
        println("Client ${id} connected to socket")
        readerStream = BufferedReader(InputStreamReader(socket.getInputStream()))
        writerStream = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())))

        start()
    }

    override fun run() {
        try {
            while (isRunning) {
                val command = readerStream.readLine()
                if (command==null) isRunning = false

                println("Command: ${command}")

                when (command) {
                    IN_SIGN_IN -> {
                        in_signIn()
                    }
                    IN_EXIT -> {
                        in_exit()
                    }
                    IN_REQUEST_TO_PLAY -> {
                        in_requestToPlay()
                    }
                    IN_RESPONSE_ON_REQUEST_TO_PLAY -> {
                        in_responseOnRequestToPlay()
                    }
//                    IN_READY_TO_PLAY -> {
//                        in_readyToPlay()
//                    }
                    IN_GET_CLIENTS_LIST -> {
                        in_getClientsList()
                    }
                }
            }

            serverInteraction.notifyRemove(clientName)
            println("Client ${clientName} disconnected")
        } catch (e: Exception) {
        }
    }

    private fun in_getClientsList() {
        println("Client ${clientName} in_getClientsList")
        out_sendListOfClient()
    }

//    private fun in_readyToPlay() {
//        val opponentName = readerStream?.readLine()
//        println("Client ${clientName} in_readyToPlay from $opponentName")
//        serverInteraction.sendReadyToPlay(clientName, opponentName)
//    }

    private fun in_responseOnRequestToPlay() {
        println("Client ${clientName} in_responseOnRequestToPlay")
        val targetClient = readerStream.readLine()
        val response = readerStream.readLine()
        if (response==ACCEPT) {
            opponent = targetClient
            out_connectPlayerToGame(targetClient)
            serverInteraction.sendReadyToPlay(clientName, targetClient)
        } else {
            serverInteraction.sendDeclineToRequest(clientName, targetClient)
        }
    }

    private fun in_requestToPlay() {
        val targetClient = readerStream.readLine()
        println("Client ${clientName} in_requestToPlay to ${targetClient}")
        serverInteraction.sendRequestToPlay(clientName, targetClient)
        //out_requestToPlay(targetClient)
    }

    private fun in_exit() {
        println("Client ${clientName} in_exit")
        isRunning = false
    }

    private fun in_signIn() {
        println("Client ${id} in_signIn")
        val login = readerStream.readLine()
        println("Client $login")
        out_sendLoginResult(login, serverInteraction.isExist(login))
    }

    fun out_sendLoginResult(login: String, isExist: Boolean) {
        println("Client ${id} out_sendLoginResult")
        writerStream.println(OUT_SEND_LOGIN_RESULT)
        writerStream.println(if (!isExist) ACCEPT else DECLINE)
        writerStream.println(login)
        writerStream.flush()

        if (!isExist) {
            clientName = login
            serverInteraction.notifyAdd(clientName)
        }
    }

    fun out_sendListOfClient() {
        println("Client ${clientName} out_sendListOfClient")
        val list = serverInteraction.getListOfClient(clientName)
        val listOfClients = StringBuilder()
        list.forEach {
            if (it!=clientName)
                listOfClients.append(it).append(" ")
        }
        val result = listOfClients.toString().trim()
        if (result.isNotEmpty()) {
            writerStream.println(OUT_SEND_LIST_OF_CLIENT)
            writerStream.println(result)
            writerStream.flush()
        }
    }

    fun out_sendConnectedClient(connectedClientName: String) {
        println("Client ${clientName} out_sendConnectedClient")
        writerStream.println(OUT_SEND_CLIENT_CONNECTED)
        writerStream.println(connectedClientName)
        writerStream.flush()
    }

    fun out_sendRemovedClient(removedClientName: String) {
        println("Client ${clientName} out_sendRemovedClient")
        writerStream.println(OUT_SEND_CLIENT_REMOVED)
        writerStream.println(removedClientName)
        writerStream.flush()
    }

    fun out_requestToPlay(requestingClientName: String) {
        println("Client ${clientName} out_requestToPlay")
        writerStream.println(OUT_REQUEST_TO_PLAY)
        writerStream.println(requestingClientName)
        writerStream.flush()
    }

    fun out_connectPlayerToGame(opponent: String) {
        println("Client ${clientName} out_connectPlayerToGame")
        writerStream.println(OUT_CONNECT_PLAYER_TO_GAME)
        writerStream.println(opponent)
        writerStream.flush()
    }

    fun out_declineRequstToPlay(opponent: String) {
        println("Client ${clientName} out_declineRequstToPlay")
        writerStream.println(OUT_DECLINE_REQUEST_TO_PLAY)
        writerStream.println(opponent)
        writerStream.flush()
    }

    fun out_yourTurn() {
        println("Client ${clientName} out_yourTurn")
        with(writerStream) {
            println(OUT_YOUR_TURN)
            flush()
        }
    }
/*
    private fun loginResult() {
        with(readerStream) {
            val login = readLine()
            if (serverInteraction.isExist(login)){

            }
        }
    }*/

    interface ServerInteraction {
        fun isExist(clientName: String): Boolean
        fun getListOfClient(clientName: String): ArrayList<String>
        fun connectPlayerToGame(opponent: String)
        fun notifyRemove(clientName: String)
        fun notifyAdd(clientName: String)
        fun sendRequestToPlay(from: String, targetName: String)
        fun sendDeclineToRequest(from: String, targetName: String)
        fun sendReadyToPlay(from: String, targetName: String)
    }
}