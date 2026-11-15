
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import bd.Conexao;
import model.Usuario;

public class UsuarioDAO {

    // LOGIN
    public Usuario logar(String email, String senha)
            throws SQLException {

        String sql = "SELECT id, nome, email, senha, perfil "
                   + "FROM usuarios "
                   + "WHERE email = ? AND ativo = true";

        try (Connection conexao = Conexao.obterConexao();
             PreparedStatement logar = conexao.prepareStatement(sql)) {

            logar.setString(1, email);

            try (ResultSet resultado = logar.executeQuery()) {

                if (resultado.next()) {

                    String senhaHash = resultado.getString("senha");

                    if (senha != null && senhaHash != null
                            && BCrypt.checkpw(senha, senhaHash)) {

                        Usuario usu = new Usuario();

                        usu.setId(resultado.getInt("id"));
                        usu.setNome(resultado.getString("nome"));
                        usu.setEmail(resultado.getString("email"));
                        usu.setPerfil(resultado.getString("perfil"));

                        return usu;
                    }
                }
            }
        }

        return null;
    }

    // CADASTRO
    public boolean cadastrar(String nome, String email,
                             String senha, String perfil)
            throws SQLException {

        String sql = "INSERT INTO usuarios "
                   + "(nome, email, senha, perfil, ativo) "
                   + "VALUES (?, ?, ?, ?, true)";

        // Protege a senha antes de salvar no banco.
        String senhaHash = BCrypt.hashpw(
                senha,
                BCrypt.gensalt(12)
        );

        try (Connection conexao = Conexao.obterConexao();
             PreparedStatement cadastro = conexao.prepareStatement(sql)) {

            cadastro.setString(1, nome);
            cadastro.setString(2, email);
            cadastro.setString(3, senhaHash);
            cadastro.setString(4, perfil);

            return cadastro.executeUpdate() > 0;
        }
    }
}
