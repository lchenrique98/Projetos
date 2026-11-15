package bd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexões com o MySQL.
 *
 * As credenciais NÃO ficam no código. Elas são lidas, nesta ordem, de:
 *   1. variáveis de ambiente DB_URL, DB_USUARIO e DB_SENHA;
 *   2. o arquivo src/main/resources/db.properties (que não vai para o Git).
 *
 * Use o db.properties.exemplo como modelo.
 *
 * O mesmo arquivo guarda o código de cadastro de técnicos
 * (tecnico.codigo / variável TECNICO_CODIGO).
 */
public class Conexao {

    private static final String URL_PADRAO =
        "jdbc:mysql://localhost:3306/helpdesk?serverTimezone=America/Sao_Paulo";

    private static final Properties CONFIG = carregarArquivo();

    private Conexao() {
    }

    public static Connection obterConexao() throws SQLException {
        String url = ler("DB_URL", "db.url", URL_PADRAO);
        String usuario = ler("DB_USUARIO", "db.usuario", null);
        String senha = ler("DB_SENHA", "db.senha", null);

        if (usuario == null || senha == null) {
            throw new SQLException(
                "Credenciais do banco não configuradas. Crie o arquivo "
                + "src/main/resources/db.properties (veja "
                + "db.properties.exemplo) ou defina DB_USUARIO e DB_SENHA."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL não encontrado no projeto.", e);
        }

        return DriverManager.getConnection(url, usuario, senha);
    }

    /**
     * Lê uma configuração: primeiro da variável de ambiente,
     * depois do db.properties. Retorna null se não existir.
     */
    public static String configuracao(String variavelAmbiente,
                                      String chaveArquivo) {
        String valor = ler(variavelAmbiente, chaveArquivo, null);
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String ler(String variavelAmbiente, String chaveArquivo,
                              String padrao) {
        String valor = System.getenv(variavelAmbiente);
        if (valor != null && !valor.isBlank()) {
            return valor;
        }
        return CONFIG.getProperty(chaveArquivo, padrao);
    }

    private static Properties carregarArquivo() {
        Properties props = new Properties();
        try (InputStream in = Conexao.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Não foi possível ler db.properties: "
                    + e.getMessage());
        }
        return props;
    }
}
