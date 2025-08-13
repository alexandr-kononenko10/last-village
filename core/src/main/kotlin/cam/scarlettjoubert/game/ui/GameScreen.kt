package cam.scarlettjoubert.game.ui

import cam.scarlettjoubert.game.Main
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.app.clearScreen
import kotlin.math.floor

class GameScreen(private val game: Main) : KtxScreen {
    private val tileSize = 64f
    private val gridW = 20                 // 20 * 64 = 1280
    private val gridH = 11                 // 11 * 64 = 704 (почти вся высота 720)
    private val worldWidth = gridW * tileSize
    private val worldHeight = gridH * tileSize

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(worldWidth, worldHeight, camera)
    private val batch: SpriteBatch = game.batch

    // --- Текстура 1x1 (белая) для заливки прямоугольников цветом batch'а ---
    private val whiteTex: Texture by lazy { makeWhiteTexture() }

    // --- Сетка занятости: true = занято (здание) ---
    private val occupied = Array(gridH) { BooleanArray(gridW) { false } }

    // Для предпросмотра места под здание (на ПК) — координата верхнего-левого тайла 2x2
    private var hoverX: Int = -1
    private var hoverY: Int = -1

    override fun show() {
        // Центрируем камеру на мире (камера статична)
        camera.position.set(worldWidth / 2f, worldHeight / 2f, 0f)
        camera.update()

        // Обработчик ввода: ставим здание 2x2 по тапу/клику
        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                val world = viewport.unproject(Vector2(screenX.toFloat(), screenY.toFloat()))
                val gx = floor(world.x / tileSize).toInt()
                val gy = floor(world.y / tileSize).toInt()
                placeBuildingIfPossible(gx, gy)
                return true
            }
        }
    }

    private fun placeBuildingIfPossible(gx: Int, gy: Int) {
        // Проверяем, помещается ли 2x2 внутри сетки
        if (gx !in 0 until gridW - 1 || gy !in 0 until gridH - 1) return

        // Проверяем, свободны ли четыре клетки
        if (!occupied[gy][gx] &&
            !occupied[gy][gx + 1] &&
            !occupied[gy + 1][gx] &&
            !occupied[gy + 1][gx + 1]
        ) {
            // Помечаем клетки как занятые
            occupied[gy][gx] = true
            occupied[gy][gx + 1] = true
            occupied[gy + 1][gx] = true
            occupied[gy + 1][gx + 1] = true
        }
    }

    override fun render(delta: Float) {
        // Обновляем "наведение" для предпросмотра (на ПК с мышью)
        updateHoverFromPointer()

        clearScreen(0.08f, 0.08f, 0.09f)  // тёмный фон
        batch.projectionMatrix = camera.combined
        batch.begin()

        // Рисуем свободные клетки серым, занятые — тёмным
        drawGrid()

        // Рисуем предпросмотр 2x2 (зелёный — можно, красный — нельзя)
        drawPreview2x2()

        // Подпись
        game.defaultFont.draw(batch, "Tap/click: поставить здание 2x2", 12f, worldHeight - 12f)

        batch.end()
    }

    private fun drawGrid() {
        // небольшой отступ внутри клетки, чтобы был виден "grid"
        val pad = 2f
        for (y in 0 until gridH) {
            for (x in 0 until gridW) {
                val px = x * tileSize + pad
                val py = y * tileSize + pad
                val w = tileSize - pad * 2
                val h = tileSize - pad * 2

                if (occupied[y][x]) {
                    tintedRect(px, py, w, h, Color(0.18f, 0.2f, 0.22f, 1f)) // занято — темнее
                } else {
                    tintedRect(px, py, w, h, Color(0.35f, 0.37f, 0.4f, 1f))  // свободно — светлее
                }
            }
        }
    }

    private fun drawPreview2x2() {
        if (hoverX < 0 || hoverY < 0) return
        val canPlace = canPlace2x2(hoverX, hoverY)
        val color = if (canPlace) Color(0f, 1f, 0f, 0.25f) else Color(1f, 0f, 0f, 0.25f)
        val px = hoverX * tileSize
        val py = hoverY * tileSize
        tintedRect(px, py, tileSize * 2f, tileSize * 2f, color)
    }

    private fun canPlace2x2(gx: Int, gy: Int): Boolean {
        if (gx !in 0 until gridW - 1 || gy !in 0 until gridH - 1) return false
        return !occupied[gy][gx] &&
            !occupied[gy][gx + 1] &&
            !occupied[gy + 1][gx] &&
            !occupied[gy + 1][gx + 1]
    }

    private fun updateHoverFromPointer() {
        // На мобильных это просто будет последняя позиция пальца при движении (если нужно — можно игнорить)
        val mx = Gdx.input.x
        val my = Gdx.input.y
        val world = viewport.unproject(Vector2(mx.toFloat(), my.toFloat()))
        val gx = floor(world.x / tileSize).toInt()
        val gy = floor(world.y / tileSize).toInt()
        if (gx in 0 until gridW && gy in 0 until gridH) {
            hoverX = gx.coerceAtMost(gridW - 2) // чтобы превью не выходило за край
            hoverY = gy.coerceAtMost(gridH - 2)
        } else {
            hoverX = -1
            hoverY = -1
        }
    }

    private fun tintedRect(x: Float, y: Float, w: Float, h: Float, color: Color) {
        val old = batch.color
        batch.color = color
        batch.draw(whiteTex, x, y, w, h)
        batch.color = old
    }

    private fun makeWhiteTexture(): Texture {
        val pm = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pm.setColor(Color.WHITE)
        pm.fill()
        val tex = Texture(pm)
        pm.dispose()
        return tex
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true) // камера остаётся центрированной
    }

    override fun dispose() {
        // Важно: не dispose'им batch и font — они принадлежат игре
        whiteTex.dispose()
    }
}
