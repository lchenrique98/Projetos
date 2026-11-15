package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Textos exibidos na tela para os códigos guardados no banco
 * (status, prioridade) e formatação de datas.
 */
public final class Rotulos {

    private Rotulos() {
    }

    /** Status na ordem do fluxo de atendimento. */
    public static final Map<String, String> STATUS = new LinkedHashMap<>();
    static {
        STATUS.put("ABERTA", "Aberta");
        STATUS.put("EM_ANALISE", "Em análise");
        STATUS.put("EM_ATENDIMENTO", "Em atendimento");
        STATUS.put("AGUARDANDO_USUARIO", "Aguardando usuário");
        STATUS.put("RESOLVIDA", "Resolvida");
        STATUS.put("CANCELADA", "Cancelada");
    }

    public static final Map<String, String> PRIORIDADE = new LinkedHashMap<>();
    static {
        PRIORIDADE.put("URGENTE", "Urgente");
        PRIORIDADE.put("ALTA", "Alta");
        PRIORIDADE.put("MEDIA", "Média");
        PRIORIDADE.put("BAIXA", "Baixa");
    }

    public static String status(String codigo) {
        return STATUS.getOrDefault(codigo, codigo == null ? "" : codigo);
    }

    public static String prioridade(String codigo) {
        return PRIORIDADE.getOrDefault(codigo, codigo == null ? "" : codigo);
    }

    /** Classe CSS do selo (ex.: "status-em-atendimento"). */
    public static String css(String prefixo, String codigo) {
        if (codigo == null) {
            return prefixo;
        }
        return prefixo + "-" + codigo.toLowerCase().replace('_', '-');
    }

    public static String dataHora(Date data) {
        if (data == null) {
            return "—";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(data);
    }
}
