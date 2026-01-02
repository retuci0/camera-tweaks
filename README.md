# jenny's mod 2 100% real

mod para Minecraft Fabric 1.21.10 que añade algunas cosas QOL, mayormente relacionadas a la cámara.

## instalación

### para gente normal:
1. ir a [lanzamientos](https://github.com/retuci0/camera-tweaks/releases)
2. selecionar la versión más reciente
3. descargar el .jar
4. moverlo a tu directorio de mods (probablemente `%appdata%/.minecraft/mods`)

asegúrate de tener una version de Minecraft y Fabric compatibles con el mod (MC `1.21.10`; Fabric `0.138.3`)
es posible que haya problemas al usarlo junto a otros mods, especialmente junto a otros clientes, pues pueden conflictar entre ellos.

### para frikis de 200 de iq
1. clona el respositorio (`git clone https://github.com/retuci0/camera-tweaks`)
2. entra al directorio del repositorio (`cd camera-tweaks`)
3. `gradlew build` para Windows o `chmod +x gradlew` + `./gradlew build` para Linux (Mac es Linux)
4. el resultado estará en `/build/libs/camtweaks-X.X.jar` (el que no lleva "sources" en el nombre)

## uso

`shift derecho` por defecto para abrir la interfaz

`pág. arriba` por defecto para abrir el editor del hud

`$` como prefijo por defecto para comandos (se autocompletan)

algunos módulos tienen teclas asignadas por defecto, asígnales `ESC` para desactivar sus atajos de teclado

## interfaz

- clic izquierdo para activar / desactivar módulos
- clic derecho para abrir sus ajustes
- clic izquierdo + arrastrar para mover marcos
- clic derecho en la cabeza del marco para expandirlo / contraerlo o para cerrarlo
- clic izquierdo sobre ajustes para modificarlos
- shift + clic derecho sobre ajustes para restablecerlos
- posiciona el puntero del ratón sobre un módulo o ajuste para leer su descripción

### protips
- shift mientras pasas el puntero por encima de un ajuste de modos para ver los modos disponibles
- clic derecho para ciclar hacia atrás en ajustes de modo
- usa la rueda del ratón para mover la interfaz verticalmente
- shift + rueda del ratón para mover todos los marcos a un rango visible en la pantalla

## capturas

<img src="screenshots/ui.png" alt="interfaz">
<img src="screenshots/nametags.png" alt="nametags mostrando daño de un tridente">
<img src="screenshots/chatplus.png" alt="chatplus y capturas en uso">
<img src="screenshots/crits.png" alt="críticos">
<img src="screenshots/fullbright.png" alt="fullbright en modo gamma cambiando el color de los shaders">
<img src="screenshots/hud.png" alt="HUD">
<img src="screenshots/hudeditor.png" alt="editor de HUD">
<img src="screenshots/colorpicker.png" alt="selector de colores">

## contibuciones

sí por favor

## licencia

no sé, haz lo que quieras con esto, es puta basura xd

aunque estaría guay que acreditases

pero bueno

## descargo de responsabilidad

me importa una puta mierda lo que pase al usar este mod en servers donde es baneable y así.

## mods recomendados

> mods que recomiendo usar junto a este:

- [fabric api (necesario)](https://modrinth.com/mod/fabric-api/): necesario para el mod
- [sodium](https://modrinth.com/mod/sodium): mejora el rendimiento general del juego
- [viafabricplus](https://modrinth.com/mod/viafabricplus): te permite conectarte a servidores que tengan una versión distinta a la de tu cliente
- [xaero's world map](https://modrinth.com/mod/xaeros-world-map/) y [xaero's minimap](https://modrinth.com/mod/xaeros-minimap/): mapa chulo
- [mod menu](https://modrinth.com/mod/modmenu): lista de los mods instalados, y sus ajustes
- [iris](https://modrinth.com/mod/iris): shaders
- [lambdynamiclights](https://modrinth.com/mod/lambdynamiclights): iluminación dinámica
- [appleskin](https://modrinth.com/mod/appleskin/): información relacionada con la comida
- [baritone (desactualizado)](https://github.com/cabaletta/baritone): google maps para juego de cubos