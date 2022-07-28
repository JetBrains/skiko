import Foundation
import UIKit

class MyViewController: UIViewController {
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil)
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
    }

    override func loadView() {
        let textField = MyTextField(frame: CGRect(x: 10, y: 10, width: 200, height: 50))
        textField.text = "Hello TextField"
        textField.contentVerticalAlignment = .top
        let textField2 = MyTextField2(frame: CGRect(x: 10, y: 10, width: 200, height: 50))
        self.view = textField
    }
}
