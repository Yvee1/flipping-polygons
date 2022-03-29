import org.openrndr.KEY_SPACEBAR
import org.openrndr.MouseButton
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.LineSegment

fun main() = application {
    configure {

    }

    oliveProgram {
        // Radius of circles representing points
        val r = 5.0

        // Start and end of the line
        var lStart = Vector2(100.0, 100.0)
        var lEnd = Vector2(300.0, 300.0)

        // List of points representing the input polygon
        val polypoints = mutableListOf<Vector2>()

        // Event listeners and variables that allow points to be added and dragged around
        var selected = -1
        mouse.buttonDown.listen {
            if (it.button == MouseButton.LEFT) {
                if (mouse.position in Circle(lStart, r*2)) {
                    selected = 0
                }
                if (mouse.position in Circle(lEnd, r*2)) {
                    selected = 1
                }
                for (i in polypoints.indices) {
                    if (mouse.position in Circle(polypoints[i], r*2)) {
                        selected = i + 2
                        break
                    }
                }
                if (selected == -1) {
                    polypoints.add(mouse.position)
                }
            }
            if (it.button == MouseButton.RIGHT) {
                for (i in polypoints.indices) {
                    if (mouse.position in Circle(polypoints[i], r*2)) {
                        polypoints.removeAt(i)
                        break
                    }
                }
            }
        }

        mouse.dragged.listen {
            if (selected == 0){
                lStart = mouse.position
            }
            if (selected == 1){
                lEnd = mouse.position
            }
            if (selected >= 2){
                polypoints[selected-2] = mouse.position
            }
        }

        mouse.buttonUp.listen {
            selected = -1
        }

        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR){
                var bestStart = Vector2.ZERO
                var bestEnd = Vector2.ZERO
                var bestD = Double.POSITIVE_INFINITY
                repeat(100000){
                    lStart = Vector2.uniform(drawer.bounds)
                    lEnd = Vector2.uniform(drawer.bounds)
                    val newD = polypoints.maxOfOrNull { LineSegment(lStart, lEnd).distance(it) } ?: 0.0
                    if (newD < bestD){
                        bestD = newD
                        bestStart = lStart
                        bestEnd = lEnd
                    }
                }
                lStart = bestStart
                lEnd = bestEnd
            }
        }

        extend {
            drawer.apply {
                clear(ColorRGBa.WHITE)

                // Draw input polygon
                fill = ColorRGBa.WHITE
                stroke = ColorRGBa.BLACK
                if (polypoints.isNotEmpty()) {
                    // Polygon outline
                    lineLoop(polypoints)
                    // Vertices
                    circles(polypoints, r)
                }

                // Draw line
                fill = ColorRGBa.WHITE
                stroke = ColorRGBa.RED
                lineSegment(lStart, lEnd)
                circle(lStart, r)
                circle(lEnd, r)

                // Comment the following lines to not draw the circles around vertices
                fill = null
                stroke = ColorRGBa.GRAY.opacify(0.5)
                circles(polypoints, polypoints.maxOfOrNull { LineSegment(lStart, lEnd).distance(it) } ?: 0.0)

                // Show distance from line to polygon as text on the screen
                fontMap = loadFont("data/fonts/default.otf", 18.0)
                fill = ColorRGBa.BLACK
                text("${polypoints.maxOfOrNull { LineSegment(lStart, lEnd).distance(it) } ?: 0.0}", 0.0, 15.0)
            }
        }
    }
}