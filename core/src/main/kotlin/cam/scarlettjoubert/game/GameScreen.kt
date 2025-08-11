package cam.scarlettjoubert.game

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import ktx.app.KtxScreen
import ktx.app.clearScreen
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
    data class SpriteComponent(var texture: com.badlogic.gdx.graphics.Texture? = null) : Component

    // Система рендеринга
    inner class RenderSystem : com.badlogic.ashley.core.EntitySystem() {
        private val family = com.badlogic.ashley.core.Family.all(PositionComponent::class.java, SpriteComponent::class.java).get()

        override fun update(deltaTime: Float) {
            batch.projectionMatrix = camera.combined
            batch.use {
                for (entity in engine.getEntitiesFor(family)) {
                    val pos = entity.getComponent(PositionComponent::class.java)
                    val sprite = entity.getComponent(SpriteComponent::class.java)
                    sprite.texture?.let { texture -> it.draw(texture, pos.x, pos.y) }
                }
            }
        }
    }

    init {
        // Инициализация сущностей
        val survivor = engine.createEntity()
        survivor.add(PositionComponent(100f, 100f))
        survivor.add(SpriteComponent(com.badlogic.gdx.graphics.Texture("survivor.png"))) // Добавь survivor.png в assets
        engine.addEntity(survivor)

        // Добавление систем
        engine.addSystem(RenderSystem())
    }

    override fun render(delta: Float) {
        clearScreen(red = 0f, green = 0f, blue = 0f) // Черный фон
        camera.update()
        mapRenderer.setView(camera)
        mapRenderer.render()
        engine.update(delta)
    }

    override fun dispose() {
        batch.disposeSafely()
        map.disposeSafely()
    }
}
