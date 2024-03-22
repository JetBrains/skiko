package org.jetbrains.skiko.sample

import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoUIView
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIViewController

class IosClocks(skiaLayer: SkiaLayer) : Clocks(skiaLayer.renderApi) {
    val viewController: UIViewController
    init {
        val view = SkikoUIView(skiaLayer)
        view.translatesAutoresizingMaskIntoConstraints = false
        viewController = UIViewController()
        viewController.view.addSubview(view)
        NSLayoutConstraint.activateConstraints(listOf(
            view.topAnchor.constraintEqualToAnchor(viewController.view.topAnchor),
            view.bottomAnchor.constraintEqualToAnchor(viewController.view.bottomAnchor),
            view.leadingAnchor.constraintEqualToAnchor(viewController.view.leadingAnchor),
            view.trailingAnchor.constraintEqualToAnchor(viewController.view.trailingAnchor)
        ))
    }
}
