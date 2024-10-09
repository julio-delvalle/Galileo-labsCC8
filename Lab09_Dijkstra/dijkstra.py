##############################
# Las funciones de este archivo 
# son una sugerenica de implementación 
# para el algoritmo dijkstra. 
#####

'''
Función de costo desde w a v, donde w y v son vecinos.
Retorna  c(w, v) el calculo de resultante de los vecinos w a v
'''
def c(w, v):
	print("Implementar / Eliminar ")


'''
Función de Menor costo desde el nodo de origen hasta el destino v.
Retorna  D(v) su costo actual de v
'''
def D(v):
	print("Implementar / Eliminar ")


'''
Función de Nodo anterior vecino de v
Retorna  p(v)  el nombre del nodo anterior vecino de v
'''
def p(v):
	print("Implementar / Eliminar ")


'''
Función donde Inicia la Ejecución del Algoritmo de Dijkstra
Retorna una Estructura 
'''
def init (initNode, routeDict):

	print("\n ======== INICIO ALGORITMO DIJKSTRA ======== \n")
	print(routeDict)

	allNodes = []
	currentNode = initNode
	for key in routeDict:
		allNodes.append(key)
	allNodes.sort()



	# Inicialización :
	nPrima = []
	nPrima.append(initNode)


	tableRow = []
	tableRow.append(0) #iteración 0

	for node in allNodes:
		if(routeDict[currentNode].containsKey(node)):
			print("vecino: ", routeDict[currentNode][node])
			tableRow.append(routeDict[currentNode][node])
		else:
			tableRow.append(float('inf'))



	print(nPrima)


	print("\n ======== FIN ALGORITMO DIJKSTRA ======== \n")








