package minesweeper.domain

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import minesweeper.domain.field.Coordinate
import minesweeper.domain.field.CoordinateValue
import minesweeper.domain.field.DotStatus
import minesweeper.domain.field.Mine
import minesweeper.domain.field.NonMine
import minesweeper.domain.vo.Height
import minesweeper.domain.vo.NumberOfMine
import minesweeper.domain.vo.Width

class MineFieldTest : StringSpec({
    "지뢰 찾기를 위한 판을 생성할수 있다." {
        shouldNotThrow<Throwable> { MineField(mapOf(Coordinate(CoordinateValue(1), CoordinateValue(1)) to Mine())) }
    }

    "필드가 비어있는 지뢰판을 생성할 경우 Exception을 던진다." {
        shouldThrow<IllegalArgumentException> { MineField(emptyMap()) }
    }

    "높이, 너비, 지뢰 갯수를 입력 받아 지뢰 판을 생성할수 있다." {
        val height = Height(5)
        val width = Width(5)
        val numberOfMine = NumberOfMine(1)
        val mineCoordinate = Coordinate(
            CoordinateValue(0),
            CoordinateValue(0)
        )
        val mineCoordinates = listOf(mineCoordinate)

        val mineField = MineField.create(height, width, numberOfMine) { _, _ -> mineCoordinates }

        val mineFieldMap = mineField.toMap()
        mineFieldMap.size shouldBe 25
        mineFieldMap[mineCoordinate] shouldBe Mine()
        mineFieldMap.values.count { it == Mine() } shouldBe numberOfMine.value
        mineFieldMap.values.forAll { it.status shouldBe DotStatus.HIDDEN }
    }

    "처음 5개 필드가 지뢰인 지뢰 판을 생성할수 있다." {
        val mineCoordinates = listOf(
            Coordinate(
                CoordinateValue(0),
                CoordinateValue(0)
            ),
            Coordinate(
                CoordinateValue(1),
                CoordinateValue(0)
            ),
            Coordinate(
                CoordinateValue(2),
                CoordinateValue(0)
            ),
            Coordinate(
                CoordinateValue(3),
                CoordinateValue(0)
            ),
            Coordinate(
                CoordinateValue(4),
                CoordinateValue(0)
            )
        )

        val mineField = createMineField(mineCoordinates)

        val mineFieldMap = mineField.toMap()
        mineCoordinates.forAll {
            mineFieldMap[it] shouldBe Mine()
        }
        mineFieldMap.values.count { it == Mine() } shouldBe mineCoordinates.size
    }

    "지뢰가 아닌 필드는 주변 필드의 지뢰 개수를 가진다." {
        val mineCoordinates = listOf(Coordinate(
            CoordinateValue(0),
            CoordinateValue(0)
        ))
        val mineAroundCoordinates = listOf(
            Coordinate(
                CoordinateValue(1),
                CoordinateValue(0)
            ),
            Coordinate(
                CoordinateValue(0),
                CoordinateValue(1)
            ),
            Coordinate(
                CoordinateValue(1),
                CoordinateValue(1)
            )
        )

        val mineField = createMineField(mineCoordinates)

        mineAroundCoordinates.forAll {
            val mineFieldMap = mineField.toMap()
            (mineFieldMap[it] as NonMine).mineCount shouldBe 1
        }
    }

    "좌표를 입력받아 지뢰 유무를 검증할수 있다. - 지뢰 필드 검증" {
        val mineCoordinate = Coordinate(
            CoordinateValue(0),
            CoordinateValue(0)
        )
        val mineCoordinates = listOf(mineCoordinate)

        val mineField = createMineField(mineCoordinates)

        mineField.open(mineCoordinate) shouldBe Mine(DotStatus.OPEN)
    }

    "좌표를 입력받아 지뢰 유무를 검증할수 있다. - 지뢰가 아닌 필드 검증" {
        val mineCoordinates = listOf(Coordinate(
            CoordinateValue(0),
            CoordinateValue(0)
        ))
        val nonMineCoordinate = Coordinate(
            CoordinateValue(3),
            CoordinateValue(3)
        )

        val mineField = createMineField(mineCoordinates)

        mineField.open(nonMineCoordinate) shouldBe NonMine(0, DotStatus.OPEN)
    }

    "이미 오픈된 좌표를 오픈하게 되는 경우 Exception을 던진다." {
        val mineCoordinates = listOf(Coordinate(
            CoordinateValue(0),
            CoordinateValue(0)
        ))
        val nonMineCoordinate = Coordinate(
            CoordinateValue(3),
            CoordinateValue(3)
        )

        val mineField = createMineField(mineCoordinates)
        mineField.open(nonMineCoordinate)

        shouldThrow<IllegalArgumentException> { mineField.open(nonMineCoordinate) }
    }

    "입력받은 좌표가 지뢰판을 벗어난 경우 Exception을 던진다." {
        val mineCoordinates = listOf(Coordinate(
            CoordinateValue(0),
            CoordinateValue(0)
        ))

        val mineField = createMineField(mineCoordinates)

        shouldThrow<IllegalArgumentException> {
            mineField.open(
                Coordinate(
                    CoordinateValue(9),
                    CoordinateValue(9)
                )
            )
        }
    }

    "입력 받은 좌표가 지뢰가 아닌경우 인접한 NonMine 필드가 모두 공개된다. " {
        val mineCoordinates = listOf(
            Coordinate(
                CoordinateValue(0),
                CoordinateValue(0)
            ),
            Coordinate(
                CoordinateValue(1),
                CoordinateValue(0)
            )
        )
        val mineField = createMineField(mineCoordinates)

        mineField.open(
            Coordinate(
                CoordinateValue(1),
                CoordinateValue(1)
            )
        )

        mineField.isAllOpen shouldBe true
    }
}) {
    companion object {
        fun createMineField(mineCoordinates: List<Coordinate>): MineField {
            val height = Height(5)
            val width = Width(5)
            val numberOfMine = NumberOfMine(mineCoordinates.size)

            return MineField.create(height, width, numberOfMine) { _, _ -> mineCoordinates }
        }
    }
}
