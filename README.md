### Escuela Colombiana de Ingeniería
### Arquitecturas de Software - ARSW
## Ana Gabriela Fiquitiva Poveda y Miguel Angel Monroy Cardenas 
## Ejercicio Introducción al paralelismo - Hilos - Caso BlackListSearch

### RESPUESTAS A LAS PREGUNTAS

---

## Parte I - Introducción a Hilos en Java

### Pregunta 2.4: ¿Cómo cambia la salida al usar run() en lugar de start()? ¿Por qué?

*Respuesta:*

Cuando utilizamos start(), los tres hilos se ejecutan de manera concurrente, mostrando los números intercalados de forma impredecible (ejemplo: 0, 1, 99, 100, 2, 3, 200...). En contraste, cuando llamamos a run(), los hilos se ejecutan secuencialmente, mostrando primero todos los números del primer hilo (0-99), luego del segundo (99-199) y finalmente del tercero (200-299).

La diferencia fundamental es que start() crea un nuevo hilo de ejecución en el sistema operativo, permitiendo que múltiples hilos corran simultáneamente. Por otro lado, run() simplemente ejecuta el método como una llamada normal en el mismo hilo principal, sin crear ningún hilo nuevo ni lograr paralelismo real.

---

## Parte II.I - Pregunta de Discusión

### ¿Cómo se podría modificar la implementación para minimizar el número de consultas cuando ya se encontró el número mínimo de ocurrencias? ¿Qué elemento nuevo traería esto al problema?

*Respuesta:*

Para minimizar consultas innecesarias, se implementaría un mecanismo de terminación anticipada usando una variable compartida con un contador atómico (AtomicInteger). Cada vez que un hilo encuentra una ocurrencia, incrementa este contador y verifica si ya se alcanzó el umbral de BLACK_LIST_ALARM_COUNT. De ser así, activa una bandera de detención booleana compartida que indica a todos los hilos que deben finalizar inmediatamente.

El principal desafío es la aparición de condiciones de carrera, donde múltiples hilos intentan acceder y modificar las mismas variables simultáneamente. Esto requiere mecanismos de sincronización (bloques synchronized o locks) para garantizar que las actualizaciones sean atómicas y consistentes. Hay una compensación: se gana eficiencia al evitar consultas innecesarias, pero el código se vuelve más complejo y la sincronización añade sobrecarga que podría reducir parte del beneficio del paralelismo.

---

## Parte III - Evaluación de Desempeño

### Análisis de Resultados Experimentales

*Configuración de pruebas:*
- IP de prueba: 202.24.34.55 (dispersa en las listas)
- Sistema operativo: Windows con JVM
- Herramienta de monitoreo: jVisualVM 2.2

### Resultados obtenidos:

Se realizaron pruebas con diferentes configuraciones de hilos para evaluar el desempeño del sistema. Los resultados fueron los siguientes:

| Número de Hilos | Tiempo (ms) | Speedup | Eficiencia |
|-----------------|-------------|---------|------------|
| 1               | 179,674     | 1.0x    | 100%       |
| 10              | 81,263      | 2.21x   | 22.1%      |
| 50              | 12,647      | 14.21x  | 28.4%      |
| 100             | 9,594       | 18.73x  | 18.7%      |

### Gráfica de Desempeño:

<img width="4155" height="1462" alt="grafica_desempeño" src="https://github.com/user-attachments/assets/d914588b-1411-48ca-8612-80eec844ac19" />


*Análisis de la gráfica:*

La gráfica izquierda muestra una reducción dramática del tiempo de ejecución al aumentar el número de hilos. Se observa que:
- De 1 a 10 hilos hay una mejora significativa (reducción de ~55%)
- De 10 a 50 hilos la mejora es aún más pronunciada (reducción de ~84%)
- De 50 a 100 hilos la mejora es más moderada (reducción de ~24%)

La gráfica derecha (Speedup) compara el rendimiento real contra el ideal lineal. Se observa que el speedup real se aleja cada vez más del ideal a medida que aumentan los hilos, lo cual es esperado debido a la sobrecarga de gestión y sincronización.

---

### Análisis Detallado por Configuración:

#### *1 Hilo (Ejecución Secuencial)*

*Tiempo de ejecución: 179,674 ms (~3 minutos)*

<img width="1362" height="719" alt="VM_1_Hilo_Monitor" src="https://github.com/user-attachments/assets/6eca39f4-fd9c-4c0f-97e6-95e9f69b9628" />

*Métricas observadas:*
- *CPU usage:* 0.5% (uso muy bajo, solo un núcleo trabajando)
- *Heap usado:* ~29.5 MB
- *Threads:* 17 live, 19 total started
- *Live peak:* 19 hilos

<img width="1362" height="720" alt="VM_1_Hilo_Threads" src="https://github.com/user-attachments/assets/6e56acb3-fb8b-461b-8db4-5959946686c8" />

*Observaciones:*
- El tiempo de ejecución es el más alto (línea base)
- Uso mínimo de CPU, evidenciando que solo se utiliza un núcleo
- La memoria se mantiene estable alrededor de 30 MB
- La mayoría de hilos son daemon o del sistema, solo 1 hilo trabaja en la búsqueda

---

#### *10 Hilos*

*Tiempo de ejecución: 81,263 ms (~1.4 minutos)*
*Mejora: 2.21x más rápido que 1 hilo*

<img width="1365" height="698" alt="VM_10_Hilos_Monitor" src="https://github.com/user-attachments/assets/23d3618c-16dc-4deb-842e-81f5dfc38e89" />

*Métricas observadas:*
- *CPU usage:* 0.7%
- *Heap usado:* ~22.4 MB
- *Threads:* 17 live, 27 total started
- *Live peak:* 27 hilos

<img width="1363" height="699" alt="VM_10_Hilos_Threads" src="https://github.com/user-attachments/assets/ec74972c-fcd1-41ea-a963-ae680c633491" />


*Observaciones:*
- Reducción significativa del tiempo (55% más rápido)
- Se observa un pico en el número de hilos activos (hasta 27)
- El uso de memoria es ligeramente menor que con 1 hilo
- La eficiencia es de 22.1%, indicando que cada hilo no está siendo aprovechado al máximo
- Esto se debe probablemente a latencias de red (I/O) en las consultas a los servidores

---

#### *50 Hilos*

*Tiempo de ejecución: 12,647 ms (~12.6 segundos)*
*Mejora: 14.21x más rápido que 1 hilo*

<img width="1364" height="694" alt="VM_50_Hilos_Monitor" src="https://github.com/user-attachments/assets/0ba9c0c8-b384-4a19-a5a8-69fce6bc87f1" />

*Métricas observadas:*
- *CPU usage:* 0.7%
- *Heap usado:* ~33.6 MB
- *Threads:* 17 live, 67 total started
- *Live peak:* 67 hilos

<img width="1365" height="702" alt="VM_50_Hilos_Threads" src="https://github.com/user-attachments/assets/d5e7e73c-4d77-4856-ad35-1a0585494e20" />

*Observaciones:*
- Mejora dramática en el tiempo de ejecución (93% más rápido que 1 hilo)
- Se alcanza el pico de 67 hilos durante la ejecución
- El uso de memoria aumenta a ~34 MB
- Eficiencia de 28.4%, la mejor entre todas las configuraciones probadas
- Este parece ser el punto más cercano al óptimo para este problema

---

#### *100 Hilos*

*Tiempo de ejecución: 9,594 ms (~9.6 segundos)*
*Mejora: 18.73x más rápido que 1 hilo*

<img width="1365" height="718" alt="VM_100_Hilos_Monitor" src="https://github.com/user-attachments/assets/d9884b94-7916-4e01-a95a-5ea6679afc4a" />

*Métricas observadas:*
- *CPU usage:* 0.9%
- *Heap usado:* ~28.4 MB
- *Threads:* 17 live, 117 total started
- *Live peak:* 117 hilos

![Uploading VM_100_Hilos_Threads.png…]()

*Observaciones:*
- La mejora respecto a 50 hilos es solo de 24% (de 12.6s a 9.6s)
- Se alcanza el pico de 117 hilos
- La eficiencia baja a 18.7%, indicando rendimientos decrecientes
- Comienza a verse el efecto de la sobrecarga de gestión de hilos
- El beneficio marginal de añadir más hilos disminuye

---

### Análisis Comparativo y Conclusiones:

*1. Ley de rendimientos decrecientes:*
- De 1→10 hilos: ganancia de 1.21x (cada 10 hilos)
- De 10→50 hilos: ganancia de 12x adicional (cada 10 hilos = 3x)
- De 50→100 hilos: ganancia de 4.5x adicional (cada 10 hilos = 0.9x)

La ganancia por cada hilo adicional disminuye conforme aumenta el número total de hilos.

*2. Punto óptimo:*
La configuración de 50 hilos muestra la mejor eficiencia (28.4%) y ofrece un excelente balance entre velocidad y uso de recursos. Aunque 100 hilos es más rápido, la mejora incremental no justifica la complejidad adicional.

*3. Sobrecarga de gestión:*
El aumento en el número de hilos totales iniciados (19→27→67→117) muestra que el sistema debe gestionar más recursos. Sin embargo, los hilos "live" se mantienen relativamente constantes (~17), indicando que muchos hilos daemon y de sistema se mantienen activos.

*4. Consumo de memoria:*
El heap usado se mantiene relativamente estable entre 22-34 MB, lo que indica que la sobrecarga de memoria por hilo es manejable en este problema.

*5. Naturaleza del problema:*
El bajo uso de CPU (~1% máximo) en todas las configuraciones indica que este es un problema limitado por I/O (consultas de red a servidores), no por CPU. Por esto:
- El speedup no es lineal
- La eficiencia es relativamente baja
- Agregar más hilos ayuda hasta cierto punto, pero no compensa completamente las latencias de red

---

## Parte IV - Preguntas Teóricas

### 1. Ley de Amdahl: ¿Por qué el mejor desempeño no se logra con 500 hilos? ¿Cómo se compara con 200 hilos?

*Respuesta:*

Según la Ley de Amdahl: *S(n) = 1 / ((1-P) + P/n)*

Donde:
- S(n) = Speedup (mejora de desempeño)
- P = Fracción paralelizable del programa
- n = Número de hilos
- (1-P) = Fracción serial del programa

*¿Por qué 500 hilos no es óptimo?*

El problema principal es la sobrecarga masiva de gestión. Con 500 hilos en una CPU de 8 núcleos, hay aproximadamente 62 hilos por núcleo, lo que obliga al sistema operativo a realizar cambios de contexto constantes. El tiempo gastado alternando entre hilos puede superar el beneficio del paralelismo. Además, todos estos hilos compiten por memoria, caché y recursos del sistema, reduciendo la eficiencia general.

La Ley de Amdahl muestra que existe un límite teórico al mejoramiento alcanzable. Incluso con infinitos procesadores, la porción serial del programa (1-P) permanece constante. Comparando con 200 hilos, la diferencia de desempeño es mínima o negativa. Para ilustrar: con P=0.95 y 8 núcleos, 8 hilos logran 5.9x de mejora, 200 hilos alcanzan 16.3x, pero 500 hilos solo 17.2x. Esa ganancia adicional de 0.9x no justifica la enorme sobrecarga de 300 hilos extra. El punto óptimo está entre el número de núcleos y 2-4 veces ese número.

---

### 2. ¿Cómo se comporta la solución usando tantos hilos como núcleos comparado con usar el doble?

*Respuesta:*

Cuando usamos N hilos (igual al número de núcleos), obtenemos paralelismo real sin sobresuscripción. Cada hilo ejecuta en su propio núcleo con mínimo cambio de contexto y uso eficiente de CPU (~100%). Sin embargo, si un hilo se bloquea esperando entrada/salida, su núcleo queda ocioso y no se aprovecha el hyperthreading disponible.

Con 2N hilos (doble de núcleos), aprovechamos el hyperthreading/SMT y tenemos mejor tolerancia a bloqueos, ya que si un hilo espera, otro puede ejecutarse inmediatamente. La desventaja es una pequeña sobrecarga por cambio de contexto y competencia por recursos, pero SMT típicamente proporciona una mejora de 1.2-1.3x, no 2x.

Para BlackListSearch, que incluye consultas de red (entrada/salida), usar 2N hilos es ligeramente mejor. Un resultado típico sería que si N hilos toma 100ms, 2N hilos tome 80-90ms (mejora del 10-20%).

---

### 3. Si se usara 1 hilo en cada una de 100 máquinas vs. c hilos en 100/c máquinas, ¿se aplicaría mejor la Ley de Amdahl? ¿Se mejoraría?

*Respuesta:*

Analizando dos escenarios: el Escenario A usa 1 hilo en cada una de 100 máquinas, mientras que el Escenario B usa c hilos en 100/c máquinas (donde c = núcleos por máquina). Ambos ofrecen 100 unidades de procesamiento total, pero con características muy diferentes.

El Escenario A tiene cero sobrecarga de cambio de contexto local y cada hilo es completamente independiente. Sin embargo, sufre de sobrecarga masiva de comunicación de red al necesitar coordinar 100 máquinas diferentes, con latencias considerables para agregar resultados.

El Escenario B reduce dramáticamente la sobrecarga de red al tener menos máquinas (100/c), aprovecha completamente los recursos de cada máquina (memoria compartida local es mucho más rápida que comunicación de red), y ofrece mejor balance entre paralelismo local y distribuido. Las desventajas son el cambio de contexto local y competencia por recursos dentro de cada máquina, pero son menores comparadas con la sobrecarga de red.

Respecto a la Ley de Amdahl, teóricamente ambos tienen el mismo mejoramiento: S(100) = 1/((1-P) + P/100). Sin embargo, en la práctica debemos extender la fórmula para sistemas distribuidos: S(n) = 1/((1-P) + P/n + C(n)), donde C(n) es la sobrecarga de comunicación. En el Escenario A, C(n) es muy alta (sincronizar 100 máquinas por red), mientras que en el Escenario B, C(n) es menor (sincronizar 100/c máquinas + sobrecarga local de c hilos).

*¿Cuál configuración se mejoraría?*

El Escenario B (c hilos × 100/c máquinas) es superior principalmente porque minimiza la sobrecarga de red. Por ejemplo, con c=4: sincronizar 25 máquinas (Escenario B) es mucho más eficiente que sincronizar 100 máquinas (Escenario A). Además, aprovecha mejor los recursos al utilizar todos los núcleos disponibles en cada máquina, y la memoria compartida local es órdenes de magnitud más rápida que la comunicación de red.

Matemáticamente: Tiempo_B = Tiempo_cómputo/100 + Sobrecarga_red(100/c) + Sobrecarga_local(c). Como Sobrecarga_red >> Sobrecarga_local, entonces Tiempo_B < Tiempo_A. Para BlackListSearch, la mejor opción es c hilos en 100/c máquinas, ya que minimiza la sobrecarga de comunicación mientras maximiza el uso de recursos locales. Como regla general, es más eficiente paralelizar primero localmente (múltiples hilos por máquina) y luego distribuir, que distribuir directamente en muchos nodos con un solo hilo cada uno.

---

## Conclusiones Generales del Ejercicio

1. *El paralelismo tiene límites:* No siempre más hilos significa mejor desempeño. Nuestras pruebas demostraron que existe un punto óptimo cerca del número de núcleos disponibles.

2. *La sobrecarga importa:* El cambio de contexto y la sincronización tienen costos reales significativos. Esto se evidenció claramente en las mediciones con jVisualVM.

3. *El balance es clave:* El número óptimo de hilos depende del hardware y la naturaleza del problema. Para problemas con I/O de red como BlackListSearch, usar 2N hilos puede ser beneficioso.

4. *Sistemas distribuidos:* Añaden complejidad pero permiten mayor escalabilidad cuando se diseñan correctamente. La comunicación de red es el principal cuello de botella.

5. *Ley de Amdahl:* Útil como guía teórica, pero debe ajustarse considerando factores prácticos como la sobrecarga de comunicación y los límites físicos del hardware.

---

### Referencias

- [Threads in Java](http://beginnersbook.com/2013/03/java-threads/)
- [Ley de Amdahl](https://www.pugetsystems.com/labs/articles/Estimating-CPU-Performance-using-Amdahls-Law-619/)
- [Java Concurrency API - join()](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html)
- [Runtime API](https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html)
