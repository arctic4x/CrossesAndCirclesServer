import java.io.*
import java.net.Socket

private const val ACCEPT = "accept"
private const val DECLINE = "decline"

private const val IN_SIGN_IN = "SIGN_IN"
private const val IN_EXIT = "EXIT"
private const val IN_REQUEST_TO_PLAY = "REQUEST_TO_PLAY"
private const val IN_RESPONSE_ON_REQUEST_TO_PLAY = "RESPONSE_ON_REQUEST_TO_PLAY"
private const val IN_READY_TO_PLAY = "READY_TO_PLAY"

private const val OUT_SEND_LOGIN_RESULT = "SEND_LOGIN_RESULT"
private const val OUT_SEND_LIST_OF_CLIENT = "SEND_LIST_OF_CLIENT"
private const val OUT_SEND_CLIENT_CONNECTED = "SEND_CLIENT_CONNECTED"
private const val OUT_SEND_CLIENT_REMOVED = "SEND_CLIENT_REMOVED"
private const val OUT_REQUEST_TO_PLAY = "REQUEST_TO_PLAY"
private const val OUT_CONNECT_PLAYER_TO_GAME = "CONNECT_PLAYER_TO_GAME"

class ClientWorker(socket: Socket, val clientName: String, val serverInteraction: ServerInteraction) : Thread() {
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
                    IN_READY_TO_PLAY -> {
                        in_readyToPlay()
                    }
                }
            }

            serverInteraction.disconnected(clientName)
            println("Client ${id} disconnected")
        } catch (e: Exception) {
        }
    }

    private fun in_readyToPlay() {
        with(readerStream) {

        }
    }

    private fun in_responseOnRequestToPlay() {
        with(readerStream) {
            val targetClient = readLine()
            val response = readLine()
            if (response==ACCEPT) {
                opponent = targetClient
                out_connectPlayerToGame()
                serverInteraction.connectPlayerToGame(clientName)
            } else {
                serverInteraction.declineRequestToPlay(clientName)
            }
        }
    }

    private fun in_requestToPlay() {
        with(readerStream) {
            val targetClient = readLine()
            out_requestToPlay(targetClient)
        }
    }

    private fun in_exit() {
        isRunning = false
    }

    private fun in_signIn() {
        with(readerStream) {
            val login = readLine()
            out_sendLoginResult(login, !serverInteraction.isExist(login))
        }
    }

    private fun out_sendLoginResult(login: String, isAccept: Boolean) {
        with(writerStream) {
            println(OUT_SEND_LOGIN_RESULT)
            println(if (isAccept) ACCEPT else DECLINE)
            println(login)
            flush()
        }
    }

    private fun out_sendListOfClient() {
        val list = serverInteraction.getListOfClient()
        val listOfClients = StringBuilder()
        list.forEach {
            if (it!=clientName)
                listOfClients.append(it).append(" ")
        }
        with(writerStream) {
            println(OUT_SEND_LIST_OF_CLIENT)
            println(listOfClients.trim())
            flush()
        }
    }

    fun out_sendConnectedClient(connectedClientName: String) {
        with(writerStream) {
            println(OUT_SEND_CLIENT_CONNECTED)
            println(connectedClientName)
            flush()
        }
    }

    fun out_sendRemovedClient(removedClientName: String) {
        with(writerStream) {
            println(OUT_SEND_CLIENT_REMOVED)
            println(removedClientName)
            flush()
        }
    }

    private fun out_requestToPlay(requestingClientName: String) {
        with(writerStream) {
            println(OUT_REQUEST_TO_PLAY)
            println(requestingClientName)
            flush()
        }
    }

    private fun out_connectPlayerToGame() {
        with(writerStream) {
            println(OUT_CONNECT_PLAYER_TO_GAME)
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
        fun disconnected(clientName: String)
        fun isExist(clientName: String): Boolean
        fun getListOfClient(): ArrayList<String>
        fun connectPlayerToGame(opponent: String)
        fun declineRequestToPlay(clientName: String)
    }
}