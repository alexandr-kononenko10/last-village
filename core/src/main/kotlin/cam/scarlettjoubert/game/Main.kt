package cam.scarlettjoubert.game

import cam.scarlettjoubert.game.ui.MainMenuScreen
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.app.emptyScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use

class Main :KtxGame<KtxScreen>() {
    lateinit var batch: SpriteBatch
    lateinit var defaultFont: BitmapFont

    override fun create() {
        // Настройка горизонтального разрешения
        Gdx.graphics.setWindowedMode(1280, 720) // Фиксированное разрешение для десктопа

        batch = SpriteBatch()

        // Загрузка шрифта
        val generator = FreeTypeFontGenerator(Gdx.files.internal("fonts/DIGITALPIXELV4-REGULAR.OTF"))
        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 24
            characters =
                FreeTypeFontGenerator.DEFAULT_CHARS + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
        }
        defaultFont = generator.generateFont(parameter)
        generator.dispose()

        addScreen(MainMenuScreen(this))
        setScreen<MainMenuScreen>()
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

    override fun dispose() {
        batch.disposeSafely()
        defaultFont.disposeSafely()
    }
}
