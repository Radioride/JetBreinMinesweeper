package minesweeper

import java.lang.Exception
import kotlin.random.Random

class Mine(field: Field) {
    val x: Int
    val y: Int
    init {
        x = Random.nextInt(0, field.Width)
        y = Random.nextInt(0, field.Height)
    }
}

class Field(val Width: Int = 9, val Height: Int = 9) {
    var data: Array<CharArray> = Array(Height) { CharArray(Width) {'.'} }
    var userData: Array<CharArray> = Array(Height) { CharArray(Width) {'.'} }
    var mines: Array<Mine> = emptyArray<Mine>()
    var userMines: ArrayList<Int> = arrayListOf<Int>()

    fun userFree(x: Int, y: Int) {
        if (data[y][x] == '*') return
        if (data[y][x] in '1'..'9') {
            userData[y][x] = data[y][x]
            this.print()
            return
        }
        if (data[y][x] == 'X') {
            for (ii in 0..mines.lastIndex) {
                userData[mines[ii].y][mines[ii].x] = 'X'
            }
            this.print()
            throw Exception("You stepped on a mine and failed!")
        }
        val arrayXY: ArrayList<Int> = arrayListOf<Int>(y*Width+x)
        var idx = 0
        do {
            val x = arrayXY[idx] % Width
            val y = arrayXY[idx] / Width
            for (yy in y -1 .. y +1) {
                for (xx in x - 1..x + 1) {
                    if (xx in 0 until Width && yy in 0 until Height && !(xx == x && yy == y) && userData[yy][xx] != '/') {
                        userData[yy][xx] = data[yy][xx]
                        if (data[yy][xx] == '.') {
                            userData[yy][xx] = '/'
                            if (!arrayXY.contains(yy*Width+xx)) arrayXY.add(yy*Width+xx)
                        }
                    }
                }
            }
            idx += 1
        } while (idx < arrayXY.size)
        this.print()
    }
    fun userMine(x: Int, y: Int) {
        val mark = arrayOf('.','*')
        if (!mark.contains(userData[y][x])) return
        val idx = userMines.indexOf(y*Width+x)
        if (idx == -1) userMines.add(y*Width+x) else userMines.removeAt(idx)
        userData[y][x] = if (userData[y][x] == '.') '*' else '.'
        this.print()
    }

    fun userCmd(cmd: String, x: Int, y: Int) {
        if (cmd == "mine") userMine(x,y) else userFree(x,y)
    }

    fun genMines(count: Int) {
        mines = emptyArray<Mine>()
        var pos = intArrayOf()
        var i = 0
        do {
            val mine = Mine(this)
            if (!pos.contains(mine.y * Width + mine.x)) {
                pos += intArrayOf(mine.y * Width + mine.x)
                mines += arrayOf(mine)
                i += 1
            }
        } while (i < count)
    }
    fun setMines(visible: Boolean) {
        for (ii in 0..mines.lastIndex) {
            data[mines[ii].y][mines[ii].x] = if (visible) 'X' else '.'
        }
    }
    fun calc() {
        setMines(true)
        for (ii in 0..mines.lastIndex) {
            for (yy in mines[ii].y -1 .. mines[ii].y +1) {
                for (xx in  mines[ii].x -1..mines[ii].x +1) {
                    if (xx in 0 until Width && yy in 0 until Height
                        && !(xx == mines[ii].x && yy == mines[ii].y)
                        && data[yy][xx] != 'X') {
                        data[yy][xx] = if (data[yy][xx] == '.')  '1' else (data[yy][xx].code + 1).toChar()
                    }
                }
            }
        }
//        setMines(false)
    }
    fun checkWin(): Boolean {
        val mark = arrayOf('.','*')
        if (userData.sumOf { it.count { mark.contains(it) } } != mines.size && userMines.size != mines.size) return false
        for (ii in 0..mines.lastIndex) {
            if (!mark.contains(userData[mines[ii].y][mines[ii].x])) return false
        }
        return true
    }
    fun print(bUser: Boolean = true) {
        val tmpData = if (bUser) userData else data
        println(" │"+(1..Width).joinToString("") { it.toString() } +"│")
        println("—│"+"—".repeat(Width)+"│")
        for (i in 0..tmpData.lastIndex) {
            println("${i+1}│${tmpData[i].joinToString("")}│")
        }
        println("—│"+"—".repeat(Width)+"│")
    }
}

fun main() {
    println("How many mines do you want on the field?")
    val field = Field()
    field.genMines(readln().toInt())
    field.calc()
    field.print()
    try {
        do {
            println("Set/delete mines marks (x and y coordinates):")
            val userInput = readln().split(" ")
            field.userCmd(userInput[2],userInput[0].toInt()-1,userInput[1].toInt()-1)
            if (field.checkWin()) throw Exception("Congratulations! You found all the mines!")
        } while (true)
    } catch (e: Exception) {
        println(e.message)
    }
}
