package com.hustsimulator.context.common;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class GeometryUtilsTest {

    @Test
    void isPointInPolygon_shouldHandleSimpleSquare() {
        List<double[]> polygon = List.of(
            new double[]{0, 0},
            new double[]{10, 0},
            new double[]{10, 10},
            new double[]{0, 10}
        );

        assertThat(GeometryUtils.isPointInPolygon(5, 5, polygon)).isTrue();
        assertThat(GeometryUtils.isPointInPolygon(11, 5, polygon)).isFalse();
        assertThat(GeometryUtils.isPointInPolygon(-1, 5, polygon)).isFalse();
    }

    @Test
    void isPointInPolygon_shouldHandleConcaveShape() {
        // L-shape
        List<double[]> polygon = List.of(
            new double[]{0, 0},
            new double[]{10, 0},
            new double[]{10, 5},
            new double[]{5, 5},
            new double[]{5, 10},
            new double[]{0, 10}
        );

        assertThat(GeometryUtils.isPointInPolygon(2, 2, polygon)).isTrue();
        assertThat(GeometryUtils.isPointInPolygon(7, 7, polygon)).isFalse(); // In the "cutout"
        assertThat(GeometryUtils.isPointInPolygon(2, 7, polygon)).isTrue();
    }
}
