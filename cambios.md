# v5.2

## novedades

### adiciones

#### módulos nuevos:

### arreglos / mejoras

- `andamios` ya funciona correctamente
- autoguardado cada cierto intervalo de tiempo en vez de guardar inmediatamente tras cada cambio en los ajustes, por optimización
- el guardado se realiza desde otro hilo ahora, para evitar que el I/O bloque el hilo principal

## problemas reconocidos

- lo de siempre.
- el ítem del tótem no se renderiza en el editor del HUD si es abierto en la pantalla de inicio