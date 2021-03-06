import io.data2viz.color.EncodedColors
import io.data2viz.core.Point
import io.data2viz.core.random
import io.data2viz.force.*
import io.data2viz.viz.circle
import io.data2viz.viz.newGroup
import io.data2viz.viz.selectElement

val width = 1200.0
val height = 900.0

lateinit var forceSimulation:   ForceSimulation
val points = (0 until 2000).map { ForceNode(it, Point(random() * width, random() * height)) }

val vizColors = EncodedColors.category20c.colors
val vizCenter = Point(width / 2, height / 2)

val centerForce = forceCenter(vizCenter)
val radialForce = forceRadial {
    center = vizCenter
    radius = { _, _, _ -> 400.0 }
    strength = { _, _, _ -> 0.2 }
}
val xForce = forceX {
    x = { _, _, _ -> width / 2 }
    strength = { _, _, _ -> 0.05 }
}
val yForce = forceY {
    y = { _, _, _ -> height / 2 }
    strength = { _, _, _ -> 0.05 }
}
val nForce = forceNBody {
    strength = { _, index, _ -> -(index.toDouble() * 10.0) }
}
val collisionForce = forceCollision {
    radius = { _, index, _ -> index.toDouble() / 200.0 }
    iterations = 4
}
val linkForce = forceLink({ from, to ->
    from.index % 20 == to.index % 20
})

val root = newGroup().apply {
    forceSimulation = ForceSimulation().apply {
        addForce("radialForce", radialForce)
//        addForce("xForce", xForce)
//        addForce("yForce", yForce)
//        addForce("centerForce", centerForce)
//        addForce("nForce", nForce)
        addForce("collisionForce", collisionForce)
        addForce("linkForce", linkForce)
        nodes = points
        on(SimulationEvent.TICK, "tickEvent", ::refresh)
        on(SimulationEvent.END, "endEvent", { println("SIMULATION ENDS") })
    }
}

fun refresh(sim: ForceSimulation) {
    println("SIMULATION TICKS")
    root.selectElement(circle, sim.nodes) {
        onEnter = {
            element.apply {
                stroke = null
                radius = index.toDouble() / 200.0
                fill = vizColors[index%20]
                cx = datum.position.x
                cy = datum.position.y
            }
            root.add(element)
        }

        onUpdate = {
            element.cx = datum.position.x
            element.cy = datum.position.y
        }

        onExit = {
            root.remove(element)
        }
    }
}