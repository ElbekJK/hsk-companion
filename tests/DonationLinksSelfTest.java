import com.example.hskcompanion.DonationLinks;

public class DonationLinksSelfTest {
    public static void main(String[] args) {
        assert DonationLinks.validatedUrl("https://ko-fi.com/actual_page").equals("https://ko-fi.com/actual_page");
        assert DonationLinks.validatedUrl("https://github.com/sponsors/example").contains("/sponsors/");
        assert DonationLinks.validatedUrl("").isEmpty();
        assert DonationLinks.validatedUrl("http://example.com/a").isEmpty();
        assert DonationLinks.validatedUrl("https://bad:secret@example.com/a").isEmpty();
        assert DonationLinks.validatedUrl("https://example.com:8080/a").isEmpty();
        assert DonationLinks.validatedUrl("javascript:alert(1)").isEmpty();
        assert DonationLinks.validatedUrl("https://example.com/a\nhttps://bad.com/").isEmpty();
        assert DonationLinks.validatedUrl("https:///missinghost").isEmpty();
        System.out.println("DonationLinksSelfTest PASS: HTTPS-only, optional, no embedded secrets or ports");
    }
}
