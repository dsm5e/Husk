import Foundation
import CoreGraphics

func check(_ actual: CGFloat, _ expected: CGFloat, _ message: String) {
    precondition(abs(actual - expected) < 0.001, "\(message): \(actual) != \(expected)")
}

let bounds = CGRect(x: 0, y: 0, width: 393, height: 852)
let keyboard = CGRect(x: 0, y: 516, width: 393, height: 336)
check(KeyboardLayout.androidInset(bounds: bounds, keyboard: keyboard, webViewOwnsKeyboard: true), 0,
      "A web login must keep its Android parent at full height")
check(KeyboardLayout.androidInset(bounds: bounds, keyboard: keyboard, webViewOwnsKeyboard: false), 336,
      "An Android editor still receives its keyboard inset")
check(KeyboardLayout.bottomOverlap(bounds: bounds, keyboard: .null), 0, "Dismissal restores the full viewport")
check(KeyboardLayout.bottomOverlap(bounds: bounds, keyboard: CGRect(x: 0, y: 852, width: 393, height: 336)), 0,
      "An off-screen keyboard does not resize content")
check(KeyboardLayout.bottomOverlap(bounds: bounds, keyboard: CGRect(x: 500, y: 516, width: 393, height: 336)), 0,
      "A keyboard outside this window does not resize it")
check(KeyboardLayout.bottomOverlap(bounds: bounds, keyboard: CGRect(x: 50, y: 300, width: 280, height: 220)), 0,
      "A floating keyboard is not a bottom inset")
check(KeyboardLayout.bottomOverlap(bounds: CGRect(x: 0, y: 0, width: 200, height: 100),
                                  keyboard: CGRect(x: 0, y: -50, width: 200, height: 300)), 100,
      "Occlusion is capped at the view height")
check(KeyboardLayout.bottomOverlap(bounds: CGRect(x: 0, y: 0, width: 786, height: 1704),
                                  keyboard: CGRect(x: 0, y: 1032, width: 786, height: 672)), 672,
      "Coordinates converted into a scaled view retain their local units")
print("Keyboard layout: PASS")
