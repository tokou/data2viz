package io.data2viz.geo.projection

import io.data2viz.geo.Sphere
import io.data2viz.geo.contains
import io.data2viz.geojson.*
import io.data2viz.test.TestBase
import kotlin.math.PI
import kotlin.test.Test

class ContainsTests : TestBase() {

    val equirectangular = io.data2viz.geo.projection.equirectangularProjection() {
        scale = 900.0 / PI
        precision = .0
    }

    @Test
    fun a_sphere_contains_any_point_LEGACY() {
        contains(Sphere(), pt(.0, .0)) shouldBe true
        contains(Sphere(), pt(10000.0, .0)) shouldBe true
        contains(Sphere(), pt(10000.0, -964524.0)) shouldBe true
    }

    @Test
    fun a_point_contains_itself_and_not_some_other_point_LEGACY() {
        contains(Point(pt(.0, .0)), pt(.0, .0)) shouldBe true
        contains(Point(pt(1.0, 2.0)), pt(1.0, 2.0)) shouldBe true
        contains(Point(pt(.0, .0)), pt(.0, 1.0)) shouldBe false
        contains(Point(pt(1.0, 1.0)), pt(1.0, .0)) shouldBe false
    }

    @Test
    fun a_multipoint_contains_any_of_its_points_LEGACY() {
        val multiPoint = MultiPoint(arrayOf(pt(.0, .0), pt(1.0, 2.0)))

        contains(multiPoint, pt(.0, .0)) shouldBe true
        contains(multiPoint, pt(1.0, 2.0)) shouldBe true
        contains(multiPoint, pt(1.0, 3.0)) shouldBe false
    }

    @Test
    fun a_linestring_contains_any_point_on_the_great_circle_path_LEGACY() {
        val lineString = LineString(arrayOf(pt(.0, .0), pt(1.0, 2.0)))

        contains(lineString, pt(.0, .0)) shouldBe true
        contains(lineString, pt(1.0, 2.0)) shouldBe true

        // TODO
        /*
        test.equal(d3.geoContains({type: "LineString", coordinates: [[0, 0], [1,2]]}, d3.geoInterpolate([0, 0], [1,2])(0.3)), true);
        test.equal(d3.geoContains({type: "LineString", coordinates: [[0, 0], [1,2]]}, d3.geoInterpolate([0, 0], [1,2])(1.3)), false);
        test.equal(d3.geoContains({type: "LineString", coordinates: [[0, 0], [1,2]]}, d3.geoInterpolate([0, 0], [1,2])(-0.3)), false);
         */
    }

    @Test
    fun a_multilinestring_contains_any_point_on_one_of_its_components_LEGACY() {
        val multiLineString = MultiLineString(
            arrayOf(
                arrayOf(pt(.0, .0), pt(1.0, 2.0)),
                arrayOf(pt(2.0, 3.0), pt(4.0, 5.0))
            )
        )

        contains(multiLineString, pt(2.0, 3.0)) shouldBe true
        contains(multiLineString, pt(5.0, 6.0)) shouldBe false
    }

    @Test
    fun a_GeometryCollection_contains_a_point_LEGACY() {
        val collection = GeometryCollection(
            arrayOf(
                LineString(arrayOf(pt(-45.0, .0), pt(.0, .0))),
                LineString(arrayOf(pt(.0, .0), pt(45.0, .0)))
            )
        )

        contains(collection, pt(-45.0, .0)) shouldBe true
        contains(collection, pt(45.0, .0)) shouldBe true
        contains(collection, pt(12.0, 25.0)) shouldBe false
    }

    @Test
    fun a_feature_contains_a_point_LEGACY() {
        val feature = Feature(LineString(arrayOf(pt(.0, .0), pt(45.0, .0))))

        contains(feature, pt(45.0, .0)) shouldBe true
        contains(feature, pt(12.0, 25.0)) shouldBe false
    }

    @Test
    fun a_FeatureCollection_contains_a_point_LEGACY() {
        val featureCollection = FeatureCollection(
            arrayOf(
                Feature(LineString(arrayOf(pt(.0, .0), pt(45.0, .0)))),
                Feature(LineString(arrayOf(pt(-45.0, .0), pt(.0, .0))))
            )
        )

        contains(featureCollection, pt(45.0, .0)) shouldBe true
        contains(featureCollection, pt(-45.0, .0)) shouldBe true
        contains(featureCollection, pt(12.0, 25.0)) shouldBe false
    }

    @Test
    fun null_contains_nothing_LEGACY() {
        val featureCollection = FeatureCollection(arrayOf())

        contains(featureCollection, pt(.0, .0)) shouldBe false
        contains(featureCollection, pt(-45.0, .0)) shouldBe false
        contains(featureCollection, pt(12.0, 25.0)) shouldBe false
    }

    /*
tape("null contains nothing", function(test) {
  test.equal(d3.geoContains(null, [0, 0]), false);
  test.end();
});

tape("a Polygon contains a point", function(test) {
  var polygon = d3.geoCircle().radius(60)();
  test.equal(d3.geoContains(polygon, [1, 1]), true);
  test.equal(d3.geoContains(polygon, [-180, 0]), false);
  test.end();
});

tape("a Polygon with a hole doesn't contain a point", function(test) {
  var outer = d3.geoCircle().radius(60)().coordinates[0],
      inner = d3.geoCircle().radius(3)().coordinates[0],
      polygon = {type:"Polygon", coordinates: [outer, inner]};
  test.equal(d3.geoContains(polygon, [1, 1]), false);
  test.equal(d3.geoContains(polygon, [5, 0]), true);
  test.equal(d3.geoContains(polygon, [65, 0]), false);
  test.end();
});

tape("a MultiPolygon contains a point", function(test) {
  var p1 = d3.geoCircle().radius(6)().coordinates,
      p2 = d3.geoCircle().radius(6).center([90,0])().coordinates,
      polygon = {type:"MultiPolygon", coordinates: [p1, p2]};
  test.equal(d3.geoContains(polygon, [1, 0]), true);
  test.equal(d3.geoContains(polygon, [90, 1]), true);
  test.equal(d3.geoContains(polygon, [90, 45]), false);
  test.end();
});
     */
}