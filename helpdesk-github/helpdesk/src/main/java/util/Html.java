package util;

/**
 * Escapa texto antes de exibir no HTML, evitando XSS.
 * Uso no JSP: <%= Html.esc(os.getTitulo()) %>
 */
public final class Html {

    private Html() {
    }

    public static String esc(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor.toString();
        StringBuilder sb = new StringBuilder(texto.length() + 16);
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
