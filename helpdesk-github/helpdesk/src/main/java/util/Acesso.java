package util;

import model.OrdemServico;
import model.Usuario;

/**
 * Regras de quem pode ver e mexer em cada ordem de serviço.
 */
public final class Acesso {

    private Acesso() {
    }

    /** Técnico vê qualquer OS; solicitante só as que ele abriu. */
    public static boolean podeVer(Usuario usuario, OrdemServico os) {
        return usuario != null && os != null
                && (usuario.isTecnico() || os.getUsuarioId() == usuario.getId());
    }

    /** Página inicial de cada perfil depois do login. */
    public static String paginaInicial(Usuario usuario) {
        return usuario != null && usuario.isTecnico() ? "/painel" : "/home";
    }
}
