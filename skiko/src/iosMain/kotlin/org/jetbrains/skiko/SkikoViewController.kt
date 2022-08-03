package org.jetbrains.skiko

import kotlinx.cinterop.*
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject

@ExportObjCClass
class SkikoViewController : UIViewController {
    @OverrideInit
    constructor() : super(nibName = null, bundle = null)
    @OverrideInit
    constructor(coder: NSCoder) : super(coder)

    constructor(skikoUIView: SkikoUIView) : this() {
        this.skikoUIView = skikoUIView
    }

    private var skikoUIView: SkikoUIView? = null

    private val uiContextMenuInteraction = UIContextMenuInteraction(object : NSObject(), UIContextMenuInteractionDelegateProtocol {
        override fun contextMenuInteraction(
            interaction: UIContextMenuInteraction,
            configurationForMenuAtLocation: CValue<CGPoint>
        ): UIContextMenuConfiguration? {
            val view = interaction.view
            println("TODO GOOD contextMenuInteraction, interaction: $interaction, view: $view")
            return UIContextMenuConfiguration.configurationWithIdentifier(
                identifier = null,
                previewProvider = null,
                actionProvider = null/*object : UIContextMenuActionProvider {
                        override fun invoke(suggestedActions: List<*>?): UIMenu? {
                            println("TODO suggestedActions: $suggestedActions")
                            return null
                        }
                    }*/
            )
        }
    })

    override fun loadView() {
        if (skikoUIView == null) {
            super.loadView()
        } else {
            val uiView = UIView(CGRectMake(0.0, 0.0, 100.0, 100.0))
            uiView.backgroundColor = UIColor.blueColor
            uiView.addInteraction(uiContextMenuInteraction)
//            this.view = uiView
            val loaded = skikoUIView!!.load()
            loaded.backgroundColor = UIColor.greenColor
            loaded.addInteraction(uiContextMenuInteraction)
//            this.view = loaded

            val uiCollectionView = UICollectionView(CGRectMake(0.0, 0.0, 100.0, 200.0), UICollectionViewCompositionalLayout())
            uiCollectionView.addInteraction(uiContextMenuInteraction)
            this.view = uiCollectionView
            uiCollectionView.setBackgroundView(loaded)
        }
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        skikoUIView?.showScreenKeyboard()
    }

    // viewDidUnload() is deprecated and not called.
    override fun viewDidDisappear(animated: Boolean) {
        skikoUIView?.detach()
    }
}
