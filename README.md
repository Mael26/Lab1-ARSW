### Escuela Colombiana de Ingeniería
### Arquitecturas de Software - ARSW
## Gabriela Fiquitivia y Miguel Monroy
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


---

## Parte III - Evaluación de Desempeño

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

---

*¿Cuál configuración se mejoraría?*

El Escenario B (c hilos × 100/c máquinas) es superior principalmente porque minimiza la sobrecarga de red. Por ejemplo, con c=4: sincronizar 25 máquinas (Escenario B) es mucho más eficiente que sincronizar 100 máquinas (Escenario A). Además, aprovecha mejor los recursos al utilizar todos los núcleos disponibles en cada máquina, y la memoria compartida local es órdenes de magnitud más rápida que la comunicación de red.

Matemáticamente: Tiempo_B = Tiempo_cómputo/100 + Sobrecarga_red(100/c) + Sobrecarga_local(c). Como Sobrecarga_red >> Sobrecarga_local, entonces Tiempo_B < Tiempo_A.

Por lo que para BlackListSearch, la mejor opción es c hilos en 100/c máquinas, ya que minimiza la sobrecarga de comunicación mientras maximiza el uso de recursos locales. Como regla general, es más eficiente paralelizar primero localmente (múltiples hilos por máquina) y luego distribuir, que distribuir directamente en muchos nodos con un solo hilo cada uno.

---

## Conclusiones Generales del Ejercicio

1. *El paralelismo tiene límites:* No siempre más hilos significa mejor desempeño
2. *La sobrecarga importa:* El cambio de contexto y la sincronización tienen costos reales significativos
3. *El balance es clave:* El número óptimo de hilos depende del hardware y la naturaleza del problema
4. *Sistemas distribuidos:* Añaden complejidad pero permiten mayor escalabilidad cuando se diseñan correctamente
5. *Ley de Amdahl:* Útil como guía teórica, pero debe ajustarse considerando factores prácticos como la sobrecarga de comunicación

---

### Referencias

- [Threads in Java](http://beginnersbook.com/2013/03/java-threads/)
- [Ley de Amdahl](https://www.pugetsystems.com/labs/articles/Estimating-CPU-Performance-using-Amdahls-Law-619/)
- [Java Concurrency API - join()](https://docs.oracle.com/javase/tutorial/essential/concurrency/join.html)
- [Runtime API](https://docs.oracle.com/javase/7/docs/api/java/lang/Runtime.html)
