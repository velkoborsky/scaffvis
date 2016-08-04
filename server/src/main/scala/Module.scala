import scaffvis.hierarchy.ScaffoldHierarchy
import com.google.inject.AbstractModule
import components.ScaffoldHierarchyComponent

/*
Configure Guice dependency injection bindings
 */
class Module extends AbstractModule {
  def configure() = {

    bind(classOf[ScaffoldHierarchy])
      .to(classOf[ScaffoldHierarchyComponent])

  }
}