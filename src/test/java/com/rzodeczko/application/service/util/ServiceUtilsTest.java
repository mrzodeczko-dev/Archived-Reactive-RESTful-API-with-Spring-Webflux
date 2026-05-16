package com.rzodeczko.application.service.util;

import com.rzodeczko.domain.vo.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceUtilsTest {

    @Nested
    @DisplayName("buildPositions()")
    class BuildPositionsTests {

        @Test
        @DisplayName("Rows and columns: positions returned in row-major order")
        void shouldBuildPositionsInRowMajorOrder() {
            List<Position> positions = ServiceUtils.buildPositions(2, 3);

            assertThat(positions).containsExactly(
                    new Position(1, 1),
                    new Position(1, 2),
                    new Position(1, 3),
                    new Position(2, 1),
                    new Position(2, 2),
                    new Position(2, 3)
            );
        }

        @Test
        @DisplayName("Zero rows: empty list")
        void shouldReturnEmptyListWhenRowsEqualZero() {
            assertThat(ServiceUtils.buildPositions(0, 3)).isEmpty();
        }

        @Test
        @DisplayName("Zero columns: empty list")
        void shouldReturnEmptyListWhenColumnsEqualZero() {
            assertThat(ServiceUtils.buildPositions(3, 0)).isEmpty();
        }
    }
}
