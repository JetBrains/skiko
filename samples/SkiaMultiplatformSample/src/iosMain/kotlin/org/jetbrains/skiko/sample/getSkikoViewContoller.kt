package org.jetbrains.skiko.sample

import kotlinx.cinterop.*
import org.jetbrains.skiko.*
import org.jetbrains.skiko.sample.*
import platform.Foundation.*
import platform.UIKit.*

fun makeApp(skiaLayer: SkiaLayer) = IosClocks(skiaLayer)

fun getSkikoViewContoller(): UIViewController {
    val view = SkikoUIView(
        SkiaLayer().apply {
            skikoView = GenericSkikoView(this, makeApp(this))
        }
    )
    view.translatesAutoresizingMaskIntoConstraints = false

    val viewController = UIViewController()
    viewController.view.addSubview(view)

    NSLayoutConstraint.activateConstraints(listOf(
        view.topAnchor.constraintEqualToAnchor(viewController.view.topAnchor),
        view.bottomAnchor.constraintEqualToAnchor(viewController.view.bottomAnchor),
        view.leadingAnchor.constraintEqualToAnchor(viewController.view.leadingAnchor),
        view.trailingAnchor.constraintEqualToAnchor(viewController.view.trailingAnchor)
    ))

    return viewController
}
