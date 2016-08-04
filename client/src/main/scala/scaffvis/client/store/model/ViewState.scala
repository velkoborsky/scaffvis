package scaffvis.client.store.model

import scaffvis.client.components.ScaffoldListBox
import scaffvis.client.components.ScaffoldTreeMap.{ColorSelectFunction, SizeSelectFunction, TransformationFunction}
import scaffvis.layout.Gradient

case class ViewState(search: Option[String] = None,
                     showOnlySearchResults: Boolean = false,
                     showOnlySelected: Boolean = false,
                     showOnlySubtree: Boolean = true,

                     showScaffoldsAsList: Boolean = false,

                     scaffoldListSortOrder: ScaffoldListBox.SortOrder = ScaffoldListBox.SortOrder.default,

                     colorSelectFunction: ColorSelectFunction = ColorSelectFunction.default,
                     colorTransformationFunction: TransformationFunction = TransformationFunction.default,
                     colorGradientFunction: Gradient = Gradient.BlueYellow,

                     sizeSelectFunction: SizeSelectFunction = SizeSelectFunction.default,
                     sizeTransformationFunction: TransformationFunction = TransformationFunction.default
                    ) {
  val filter =
    VSFilter(
      search,
      showOnlySearchResults = showOnlySearchResults,
      showOnlySelected = showOnlySelected,
      showOnlySubtree = showOnlySubtree
    )

  val scaffoldTreeMap =
    VSScaffoldTreemap(
      colorSelectFunction, colorTransformationFunction, colorGradientFunction,
      sizeSelectFunction, sizeTransformationFunction
    )
}

//subsets commonly used together

case class VSFilter(
                     search: Option[String] = None,
                     showOnlySearchResults: Boolean = false,
                     showOnlySelected: Boolean = false,
                     showOnlySubtree: Boolean = true
                   )

case class VSScaffoldTreemap(
                              colorSelectFunction: ColorSelectFunction,
                              colorTransformationFunction: TransformationFunction,
                              colorGradientFunction: Gradient,
                              sizeSelectFunction: SizeSelectFunction,
                              sizeTransformationFunction: TransformationFunction
                            )