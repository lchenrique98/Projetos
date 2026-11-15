
package bd;

import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) {

        try (Connection conexao = Conexao.obterConexao()) {

            System.out.println("Conexão realizada com sucesso!");
            System.out.println("Banco: " +
                conexao.getCatalog());

        } catch (SQLException e) {

            System.out.println("Erro ao conectar com o banco:");
            e.printStackTrace();

        }
    }
}
