import UIKit
import SwiftUI
import shared

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(_ scene: UIScene, willConnectTo session: UISceneSession, options connectionOptions: UIScene.ConnectionOptions) {
        if let windowScene = scene as? UIWindowScene {
            let window = UIWindow(windowScene: windowScene)
//            window.rootViewController = SwiftHelper().getViewController()
            window.rootViewController = MyViewController()
            window.backgroundColor = .white
            self.window = window
            window.makeKeyAndVisible()
        }
    }

}
