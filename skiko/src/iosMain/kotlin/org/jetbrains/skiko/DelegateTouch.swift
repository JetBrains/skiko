import UIKit
import shared

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        window = UIWindow(frame: UIScreen.main.bounds)
        let mainViewController = Main_iosKt.MainViewController()
        window?.rootViewController = mainViewController
        window?.makeKeyAndVisible()
        let rect = CGRect(x: 0, y: 0, width: 300, height: 100)
        let textField = UITextField(frame: rect)
        textField.text = "UITextField"
        window?.rootViewController?.view.addSubview(textField)
        window?.rootViewController?.view.addSubview(DelegateView(delegateTo: textField, frame: rect))
        return true
    }
}

class DelegateView:UIView {

    var delegate:UIView

    init(delegateTo:UIView, frame:CGRect) {
        delegate = delegateTo
        super.init(frame: frame)
    }

    required init?(coder: NSCoder) {
        delegate = UIView()
        super.init(coder: coder)
    }

    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        delegate.touchesBegan(Set(touches.map { touch -> UITouch in MyTouch(delegate: delegate, base: touch) }), with: event)
    }

    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        delegate.touchesMoved(Set(touches.map { touch -> UITouch in MyTouch(delegate: delegate, base: touch) }), with: event)
    }

    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        delegate.touchesEnded(Set(touches.map { touch -> UITouch in MyTouch(delegate: delegate, base: touch) }), with: event)
    }

    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        delegate.touchesCancelled(Set(touches.map { touch -> UITouch in MyTouch(delegate: delegate, base: touch) }), with: event)
    }

    override func touchesEstimatedPropertiesUpdated(_ touches: Set<UITouch>) {
        delegate.touchesEstimatedPropertiesUpdated(Set(touches.map { touch -> UITouch in MyTouch(delegate: delegate, base: touch) }))
    }

    override func pressesBegan(_ presses: Set<UIPress>, with event: UIPressesEvent?) {
        delegate.pressesBegan(presses, with: event)
    }

    override func pressesChanged(_ presses: Set<UIPress>, with event: UIPressesEvent?) {
        delegate.pressesChanged(presses, with: event)
    }

    override func pressesEnded(_ presses: Set<UIPress>, with event: UIPressesEvent?) {
        delegate.pressesEnded(presses, with: event)
    }

    override func pressesCancelled(_ presses: Set<UIPress>, with event: UIPressesEvent?) {
        delegate.pressesCancelled(presses, with: event)
    }

    override func motionBegan(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        delegate.motionBegan(motion, with: event)
    }

    override func motionEnded(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        delegate.motionEnded(motion, with: event)
    }

    override func motionCancelled(_ motion: UIEvent.EventSubtype, with event: UIEvent?) {
        delegate.motionCancelled(motion, with: event)
    }

}

class MyTouch:UITouch {
    var delegate:UIView
    var base:UITouch
    
    init(delegate:UIView, base:UITouch) {
        self.delegate = delegate
        self.base = base
    }

    override var timestamp: TimeInterval {
        base.timestamp
    }
    override var phase: Phase {
        base.phase
    }
    override var tapCount: Int {
        base.tapCount
    }
    override var type: TouchType {
        base.type
    }
    override var majorRadius: CGFloat {
        base.majorRadius
    }
    override var majorRadiusTolerance: CGFloat {
        base.majorRadiusTolerance
    }
    override var window: UIWindow? {
        base.window
    }
    override var view: UIView? {
        delegate
    }
    override var gestureRecognizers: [UIGestureRecognizer]? {
        base.gestureRecognizers
    }

    override func location(in view: UIView?) -> CGPoint {
        base.location(in: view)
    }

    override func previousLocation(in view: UIView?) -> CGPoint {
        base.previousLocation(in: view)
    }

    override func preciseLocation(in view: UIView?) -> CGPoint {
        base.preciseLocation(in: view)
    }

    override func precisePreviousLocation(in view: UIView?) -> CGPoint {
        base.precisePreviousLocation(in: view)
    }

    override var force: CGFloat {
        base.force
    }
    override var maximumPossibleForce: CGFloat {
        base.maximumPossibleForce
    }

    override func azimuthAngle(in view: UIView?) -> CGFloat {
        base.azimuthAngle(in: view)
    }

    override func azimuthUnitVector(in view: UIView?) -> CGVector {
        base.azimuthUnitVector(in: view)
    }

    override var altitudeAngle: CGFloat {
        base.altitudeAngle
    }
    override var estimationUpdateIndex: NSNumber? {
        base.estimationUpdateIndex
    }
    override var estimatedProperties: Properties {
        base.estimatedProperties
    }
    override var estimatedPropertiesExpectingUpdates: Properties {
        base.estimatedPropertiesExpectingUpdates
    }
}
