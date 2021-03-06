package io.data2viz.examples.lineOfSight

import io.data2viz.color.colors
import io.data2viz.core.*
import io.data2viz.math.Angle
import io.data2viz.math.PI
import io.data2viz.math.deg
import io.data2viz.timer.timer
import io.data2viz.viz.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

val width = 800.0
val height = 600.0

lateinit var viz: VizContext

val polygonNb = 8
val randomPointsNb = 10
val randomPoints = List(polygonNb, { mutableListOf<Point>() })
val polygons = mutableListOf<Polygon>()
val allCorners = mutableListOf<Point>()
val allSegments = mutableListOf<Ray>()

lateinit var from: Point
lateinit var fromList: List<Point>
var xSpeed: Double = .0
var ySpeed: Double = .0

lateinit var sightGroup: Group
lateinit var pointGroup: Group

data class Ray(val from: Point, val to: Point)
data class Intersection(val point: Point, val param: Double, var angle: Angle = 0.deg)

fun VizContext.lineOfSightViz() {

    sightGroup = newGroup()
    pointGroup = newGroup()

    viz = this
    viz.add(sightGroup)
    viz.add(pointGroup)

    // drawing random points around random locations
    (0 until polygonNb).forEach { i ->
        val x0 = random() * width * .9
        val y0 = random() * height * .9
        (0 until randomPointsNb).forEach { j ->
            val x = (x0 + (random() * (width / 10))).coerceIn(.0, width)
            val y = (y0 + (random() * (height / 10))).coerceIn(.0, height)
            randomPoints[i].add(Point(x, y))
        }
    }

    // transform points in Polygons using convex hull
    randomPoints.forEach { points ->
        polygons.add(polygonHull(points)!!)
    }

    polygons.forEach { polygon ->
        path {
            var previousPoint: Point? = null
            fill = colors.black
            stroke = colors.black
            moveTo(polygon.points[0].x, polygon.points[0].y)
            polygon.points.forEachIndexed { index, point ->
                if (index != 0) lineTo(point.x, point.y)
                allCorners.add(point)

                if (previousPoint != null) allSegments.add(Ray(previousPoint!!, point))
                previousPoint = point
            }
            lineTo(polygon.points[0].x, polygon.points[0].y)
            allSegments.add(Ray(previousPoint!!, polygon.points[0]))
        }
    }

    val NW = Point(.0, .0)
    val NE = Point(width, .0)
    val SW = Point(.0, height)
    val SE = Point(width, height)

    allCorners.add(NW)
    allCorners.add(NE)
    allCorners.add(SW)
    allCorners.add(SE)

    allSegments.add(Ray(NW, NE))
    allSegments.add(Ray(NE, SE))
    allSegments.add(Ray(SE, SW))
    allSegments.add(Ray(SW, NW))

    initSpeeds()
    initStartPoint()
    fromList = listOf(from)

    timer { now ->
        loop(now)
    }
}

fun loop(now: Double) {
    movePoint()

    val sightPolygon = getSightPolygon()
    val points = sightPolygon.points

    viz.selectElement(line, sightPolygon.points) {
        onEnter = {
            element.apply {
                stroke = colors.red
                x1 = from.x
                y1 = from.y
                x2 = datum.x
                y2 = datum.y
            }
            viz.add(element)
        }

        onUpdate = {
            element.x1 = from.x
            element.y1 = from.y
            element.x2 = datum.x
            element.y2 = datum.y
        }

        onExit = {
            viz.remove(element)
        }
    }

    /*sightGroup.apply {
        path {
            moveTo(points[0].x, points[0].y)
            fill = colors.red
            stroke = colors.red
            points.forEach { point ->
                lineTo(point.x, point.y)
            }
            lineTo(points[0].x, points[0].y)
        }
    }*/

    /*pointGroup.selectElement(circle, fromList) {
        onEnter = {
            element.apply {
                stroke = colors.black
                fill = colors.black
                radius = 5.0
                cx = from.x
                cy = from.y
            }
            viz.add(element)
        }

        onUpdate = {
            element.cx = from.x
            element.cy = from.y
        }
    }*/
}

private fun getSightPolygon(): Polygon {
    val allAngles = mutableListOf<Angle>()
    allCorners.forEach {
        val rad = atan2(it.y - from.y, it.x - from.x)
        allAngles.add(Angle(rad))
        allAngles.add(Angle(rad + .00001))
        allAngles.add(Angle(rad - .00001))
    }

    val intersections = mutableListOf<Intersection>()
    allAngles.forEach { angle ->
        // Calculate dx & dy from angle
        val dx = angle.cos
        val dy = angle.sin

        // Ray from center of screen to mouse
        val ray = Ray(from, Point(from.x + dx, from.y + dy))

        // Find CLOSEST intersection
        var closestIntersection: Intersection? = null
        allSegments.forEach { segment ->
            val intersection = getIntersection(ray, segment)
            if (intersection != null && (closestIntersection == null || (intersection.param < closestIntersection!!.param))) {
                closestIntersection = intersection
            }
        }

        // Intersect angle
        if (closestIntersection != null) {
            closestIntersection!!.angle = angle

            // Add to list of intersections
            intersections.add(closestIntersection!!)
        }
    }

    // Sort intersects by angle
    intersections.sortBy { it.angle.rad }

    // Polygon is intersects, in order of angle
    return Polygon(intersections.map { it.point })
}

// Find intersection of RAY & SEGMENT
private fun getIntersection(ray: Ray, segment: Ray): Intersection? {

    // RAY in parametric: Point + Delta*T1
    val r_px = ray.from.x
    val r_py = ray.from.y
    val r_dx = ray.to.x - ray.from.x
    val r_dy = ray.to.y - ray.from.y

    // SEGMENT in parametric: Point + Delta*T2
    val s_px = segment.from.x
    val s_py = segment.from.y
    val s_dx = segment.to.x - segment.from.x
    val s_dy = segment.to.y - segment.from.y

    // Are they parallel? If so, no intersect
    val r_mag = sqrt(r_dx * r_dx + r_dy * r_dy)
    val s_mag = sqrt(s_dx * s_dx + s_dy * s_dy)
    if (r_dx / r_mag == s_dx / s_mag && r_dy / r_mag == s_dy / s_mag) {
        // Unit vectors are the same.
        return null
    }
    // SOLVE FOR T1 & T2
    // r_px+r_dx*T1 = s_px+s_dx*T2 && r_py+r_dy*T1 = s_py+s_dy*T2
    // ==> T1 = (s_px+s_dx*T2-r_px)/r_dx = (s_py+s_dy*T2-r_py)/r_dy
    // ==> s_px*r_dy + s_dx*T2*r_dy - r_px*r_dy = s_py*r_dx + s_dy*T2*r_dx - r_py*r_dx
    // ==> T2 = (r_dx*(s_py-r_py) + r_dy*(r_px-s_px))/(s_dx*r_dy - s_dy*r_dx)
    val T2 = (r_dx * (s_py - r_py) + r_dy * (r_px - s_px)) / (s_dx * r_dy - s_dy * r_dx)
    val T1 = (s_px + s_dx * T2 - r_px) / r_dx

    // Must be within parametic whatevers for RAY/SEGMENT
    if (T1 < 0) return null
    if (T2 < 0 || T2 > 1) return null
    // Return the POINT OF INTERSECTION
    return Intersection(Point(r_px + r_dx * T1, r_py + r_dy * T1), T1)
}

private fun initStartPoint() {
    from = Point(random() * width, random() * height)
    var collision = false
    polygons.forEach { polygon ->
        collision = collision || polygon.contains(from)
    }
    if (collision) initStartPoint()
}

private fun initSpeeds() {
    val angle = random() * PI * 2
    xSpeed = cos(angle) * 8
    ySpeed = sin(angle) * 8
}

private fun movePoint() {
    val newfrom = Point(from.x + xSpeed, from.y + ySpeed)

    var collision = false
    polygons.forEach { polygon ->
        collision = collision || polygon.contains(newfrom)
    }
    collision = collision || newfrom.x <= .0 || newfrom.x >= width || newfrom.y <= 0 || newfrom.y >= height

    if (collision) {
        initSpeeds()
        movePoint()
    } else {
        from = newfrom
    }
}
