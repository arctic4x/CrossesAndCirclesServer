import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket

class Main {
    companion object {
        private const val MA_PORT = 6969

        private val clientThreadPool = ArrayList<ClientWorker>()

        private var id = 0

        val clientInteraction = object : ClientWorker.ServerInteraction {
            override fun isExist(clientName: String): Boolean {
                clientThreadPool.forEach {
                    if (it.clientName.equals(clientName)) return true
                }
                return false
            }

            override fun getListOfClient(clientName: String): ArrayList<String> {
                val list = ArrayList<String>()
                clientThreadPool.forEach {
                    if (!it.clientName.equals(clientName))
                        list.add(it.clientName)
                }
                return list
            }

            override fun sendRequestToPlay(from: String, targetName: String) {
                clientThreadPool.forEach {
                    if (it.clientName.equals(targetName)) {
                        it.out_requestToPlay(from)
                        return
                    }
                }
            }

            override fun connectPlayerToGame(opponent: String) {

            }

            override fun sendDeclineToRequest(from: String, targetName: String) {
                clientThreadPool.forEach {
                    if (targetName.equals(it.clientName)) {
                        it.out_declineRequstToPlay(from)
                        return
                    }
                }
            }

            override fun notifyRemove(clientName: String) {
                var removedItem: ClientWorker? = null
                clientThreadPool.forEach {
                    if (clientName!=it.clientName) {
                        it.out_sendRemovedClient(clientName)
                    } else {
                        removedItem = it
                    }
                }
                if (removedItem!=null) clientThreadPool.remove(removedItem!!)
            }

            override fun notifyAdd(clientName: String) {
                clientThreadPool.forEach {
                    if (clientName!=it.clientName) {
                        it.out_sendConnectedClient(clientName)
                    }
                }
            }

            override fun sendReadyToPlay(from: String, targetName: String) {
                clientThreadPool.forEach {
                    if (targetName.equals(it.clientName)) {
                        it.out_connectPlayerToGame(from)
                        return
                    }
                }
            }

            override fun makeAction(opponent: String, position: Int) {
                clientThreadPool.forEach {
                    if (opponent.equals(it.clientName)) {
                        it.out_sendAction(it.myFigure, position)
                        return
                    }
                }
            }

            override fun opponentIsReady(opponent: String) {
                clientThreadPool.forEach {
                    if (opponent.contains(it.clientName)) {
                        it.opponentIsReady()
                        return
                    }
                }
            }

            override fun opponentTurn(opponent: String) {
                clientThreadPool.forEach {
                    if (opponent.equals(it.clientName)) {
                        it.out_youtTurn()
                        return
                    }
                }
            }

            override fun opponentFigure(opponent: String, figure: Int) {
                clientThreadPool.forEach {
                    if (opponent.equals(it.clientName)) {
                        it.myFigure = figure
                        return
                    }
                }
            }

            override fun getOpponent(opponent: String): ClientWorker? {
                clientThreadPool.forEach {
                    if (opponent.equals(it.clientName)) {
                        return it
                    }
                }
                return null
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println("Start server desktop application.")
            try {
                val server = ServerSocket(MA_PORT)
                val inputServerStreamReader = BufferedReader(InputStreamReader(System.`in`))

                println("Server is created.")

                while (true) {   //!server.isClosed) {
                    /*if (inputServerStreamReader.ready()) {
                        val client = server.accept()
                        println("Client is found")
                        ClientThread(client, id++)
                    }*/
                    val socket = server.accept()
                    try {
                        id++
                        val listOfCliets = StringBuilder()

                        clientThreadPool.forEach {
                            //it.addClient(id)
                            listOfCliets.append(it.id).append(" ")
                        }

                        val clientThread = ClientWorker(socket, "", clientInteraction)
                        //clientThread.out_sendListOfClient()
                        clientThreadPool.add(clientThread)
                    } catch (e: Exception) {
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}