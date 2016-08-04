package components

import javax.inject.{Inject, Singleton}

import scaffvis.stores.ScaffoldHierarchyStore
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@Singleton
class ScaffoldHierarchyComponent @Inject() (lifecycle: ApplicationLifecycle) extends ScaffoldHierarchyStore {

  open()

  lifecycle.addStopHook { () =>
    Future.successful(close())
  }

}