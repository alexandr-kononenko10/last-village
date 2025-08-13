package cam.scarlettjoubert.game.ui

import cam.scarlettjoubert.game.ui.GameScreen
import cam.scarlettjoubert.game.Main
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.ScreenViewport
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.app.clearScreen

class MainMenuScreen(private val game: Main) : KtxScreen {
    private val stage = Stage(ScreenViewport())

    override fun show() {
        Gdx.input.inputProcessor = stage
        stage.isDebugAll = true // Для отладки UI

        // Создаём таблицу для центрирования UI
        val table = Table().apply {
            setFillParent(true)
            debug = true // Для отладки таблицы
        }
        stage.addActor(table)

        // Стиль для кнопок
        val buttonStyle = TextButton.TextButtonStyle().apply {
            font = game.defaultFont
            up = createButtonBackground(Color.DARK_GRAY)
            down = createButtonBackground(Color.GRAY)
        }

        // Кнопки
        table.add(TextButton("Старт", buttonStyle).apply {
            onClick {
                game.addScreen(GameScreen(game = game))
                game.setScreen<GameScreen>() }
        }).width(300f).height(100f).pad(10f).row()

        table.add(TextButton("Настройки", buttonStyle).apply {
            onClick { println("Открыты настройки") }
        }).width(300f).height(100f).pad(10f).row()

        table.add(TextButton("Выход", buttonStyle).apply {
            onClick { Gdx.app.exit() }
        }).width(300f).height(100f).pad(10f)
    }

    override fun render(delta: Float) {
        clearScreen(red = 0f, green = 0f, blue = 0.2f) // Тёмно-синий фон
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }

    private fun createButtonBackground(color: Color): Drawable {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(color)
            fill()
        }
        val texture = Texture(pixmap)
        pixmap.dispose()
        return TextureRegionDrawable(TextureRegion(texture))
    }
}
