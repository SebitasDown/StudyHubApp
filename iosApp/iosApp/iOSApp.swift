import SwiftUI
import Shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL { url in
                handleOAuthCallback(url)
            }
        }
    }

    private func handleOAuthCallback(_ url: URL) {
        guard url.scheme == "studyhub" else { return }
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: false) else { return }
        let token = components.queryItems?.first(where: { $0.name == "token" })?.value
        let user = components.queryItems?.first(where: { $0.name == "user" })?.value
        GoogleOAuth.shared.handleCallback(token: token, userJson: user)
    }
}
