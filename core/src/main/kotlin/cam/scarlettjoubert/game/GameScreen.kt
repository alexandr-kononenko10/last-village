package cam.scarlettjoubert.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.ashley.allOf
import ktx.assets.disposeSafely
import ktx.graphics.use

class GameScreen : KtxScreen {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
        setToOrtho(false)
    }
    private val map = TmxMapLoader().load("map.tmx") // Убедись, что map.tmx есть в assets
    private val mapRenderer = OrthogonalTiledMapRenderer(map)
    private val engine = PooledEngine()

    // Компоненты
    data class PositionComponent(var x: Float = 0f, var y: Float = 0f) : Component
    data class SpriteComponent(var texture: Texture? = null) : Component

    // Система рендеринга
    inner class RenderSystem : com.badlogic.ashley.core.EntitySystem() {
        private val family = allOf(PositionComponent::class, SpriteComponent::class).get()

        override fun update(deltaTime: Float) {
            Gdx.app.log("RenderSystem", "Rendering ${engine.getEntitiesFor(family).size()} entities")
            batch.projectionMatrix = camera.combined
            batch.use {
                for (entity in engine.getEntitiesFor(family)) {
                    val pos = entity[PositionComponent::class]
                    val sprite = entity[SpriteComponent::class]
                    sprite?.texture?.let { texture -> it.draw(texture, pos!!.x, pos!!.y) }
                }
            }
        }
    }

    init {
        try {
            // Инициализация сущностей
            val survivor = engine.createEntity().add {
                component(PositionComponent(100f, 100f))
                component(SpriteComponent(Texture(Gdx.files.internal("survivor.png"))))
            }
            engine.addEntity(survivor)

            // Добавление систем
            engine.addSystem(RenderSystem())
        } catch (e: Exception) {
            Gdx.app.error("GameScreen", "Failed to initialize: ${e.message}")
        }
    }

    override fun render(delta: Float) {
        clearScreen(red = 0f, green = 0f, blue = 0f) // Чёрный фон
        camera.update()
        mapRenderer.setView(camera)
        mapRenderer.render()
        engine.update(delta)
    }

    override fun dispose() {
        batch.disposeSafely()
        map.disposeSafely()
        mapRenderer.disposeSafely()
    }
}
