import Foundation
import CoreGraphics

/// Coordinates are local to the rendered app view, including any window scaling.
enum KeyboardLayout {
    static func bottomOverlap(bounds: CGRect, keyboard: CGRect) -> CGFloat {
        let overlap = bounds.intersection(keyboard)
        // A floating keyboard cannot be represented as an Android bottom inset.
        guard !overlap.isNull, !overlap.isEmpty, keyboard.maxY >= bounds.maxY else { return 0 }
        return overlap.height
    }

    static func androidInset(bounds: CGRect, keyboard: CGRect, webViewOwnsKeyboard: Bool) -> CGFloat {
        // WKWebView already adjusts its visual viewport and scrolls the focused HTML input.
        // Shrinking its Android parent as well accounts for the same keyboard twice.
        webViewOwnsKeyboard ? 0 : bottomOverlap(bounds: bounds, keyboard: keyboard)
    }
}
