##############################
# Las funciones de este archivo 
# son una sugerenica de implementación 
# para el algoritmo dijkstra. 
#####

'''
Función de costo desde w a v, donde w y v son vecinos.
Retorna  c(w, v) el calculo de resultante de los vecinos w a v
'''
def c(w, v, routeDict):
	return routeDict[w][v]


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

	stepsCalcs = []
	allNodes = []
	currentNode = initNode
	for key in routeDict:
		allNodes.append(key)
	allNodes.sort()



	# Inicialización :
	nPrima = []
	nPrima.append(initNode)
	iterationCounter = 0

	tableRow = {}
	tableRow['iter'] = iterationCounter #iteración 0

	for node in allNodes:
		if(node != initNode):
			if(node in routeDict[currentNode]):
				print("vecino: ", routeDict[currentNode][node])
				tableRow[node] = [routeDict[currentNode][node], initNode]
			else:
				tableRow[node] = [float('inf'), initNode]

	tableRow['addedNode'] = currentNode #AL finalizar la iteración se añade el nodo que se agregó
	stepsCalcs.append(tableRow) #Primera fila completada. Agregar a stepsCalcs
	prevTableRow = tableRow #llevar linea anterior
	prevRowNodeAdded = currentNode

	print("tableRow: ", tableRow)
	print("nPrima: ", nPrima)



	while(len(nPrima) < len(allNodes)):
		iterationCounter += 1
		minNode = getMinCostNode(prevTableRow, nPrima)
		nPrima.append(minNode)
		prevRowNodeAdded = minNode
		print("se agrega a nPrima: ", minNode)
		if(len(nPrima) == len(allNodes)):
			lastTableRow = prevTableRow.copy()
			lastTableRow['addedNode'] = minNode
			stepsCalcs.append(lastTableRow)
			break
		currentNode = minNode

		tableRow = {}
		tableRow['iter'] = iterationCounter #num iteracion

		print('antes de entrar al loop: previous row: ', prevTableRow)

		for node in allNodes:
			if(node != initNode):
				if(node not in nPrima and node != initNode):
					if(node in routeDict[currentNode]):
						print('analizando vecino: ', node)
						print('costo actual: ', prevTableRow[node][0])
						print("c + D:",(c(currentNode, node, routeDict) + prevTableRow[prevRowNodeAdded][0]))

						if((c(currentNode, node, routeDict) + prevTableRow[prevRowNodeAdded][0]) <= prevTableRow[node][0]):
							tableRow[node] = [c(currentNode, node, routeDict) + prevTableRow[prevRowNodeAdded][0], currentNode]
						else:
							tableRow[node] = prevTableRow[node]
					else:
						tableRow[node] = prevTableRow[node]
				else:
					tableRow[node] = prevTableRow[node]


		tableRow['addedNode'] = currentNode #AL finalizar la iteración se añade el nodo que se agregó
		print("iteracion: ", iterationCounter, " minNode: ", minNode)
		print("nPrima: ", nPrima)
		print("tableRow: ", tableRow)
		stepsCalcs.append(tableRow) #Agregar a stepsCalcs
		prevTableRow = tableRow


	print("\n ======== FIN ALGORITMO DIJKSTRA ======== \n")
	return stepsCalcs





def getMinCostNode(tableRow, nPrima):
	minCost = float('inf')
	minNode = None
	tableRowFirstElement = next(iter(tableRow.items()))[0]
	tableRowLastElement = list(tableRow.items())[-1][0]
	print("tableRowFirstElement: ", tableRowFirstElement)
	print("tableRowLastElement: ", tableRowLastElement)

	for node in tableRow:
		print('analizando', node)
		if(node != tableRowFirstElement and node != tableRowLastElement):
			if(node not in nPrima):
				if(tableRow[node][0] < minCost):
					minCost = tableRow[node][0]
					minNode = node
		else:
			print("no se analiza")
	return minNode