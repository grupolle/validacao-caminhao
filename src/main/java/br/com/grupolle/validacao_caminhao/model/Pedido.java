package br.com.grupolle.validacao_caminhao.model;

import javax.persistence.Column;

public class Pedido {
    
    @Column(name="ordemcarga")
    private Long ordemCarga;
    
    @Column(name="codemp")
    private Long codemp;
    
    @Column(name="idrev")
    private Long idrev;
    
    @Column(name="numnota")
    private Long numnota;
       
	@Column(name="entrou_caminhao")
    private String entroucaminhao;
	
	@Column(name="sequencia")
    private long sequencia;
	
	@Column(name="exigeconf")
    private String exigeconf;
	
	  @Column(name="volumoso") 
	    private String volumoso;

    // Getters e Setters

	public String getVolumoso() {
		return volumoso;
	}

	public void setVolumoso(String volumoso) {
		this.volumoso = volumoso;
	}

	public String getExigeconf() {
		return exigeconf;
	}

	public void setExigeconf(String exigeconf) {
		this.exigeconf = exigeconf;
	}

	public Long getOrdemCarga() {
        return ordemCarga;
    }

    public long getSequencia() {
		return sequencia;
	}

	public void setSequencia(long sequencia) {
		this.sequencia = sequencia;
	}

	public void setOrdemCarga(Long ordemCarga) {
        this.ordemCarga = ordemCarga;
    }

    public Long getCodemp() {
        return codemp;
    }

    public void setCodemp(Long codemp) {
        this.codemp = codemp;
    }

    public Long getIdrev() {
        return idrev;
    }

    public void setIdrev(Long idrev) {
        this.idrev = idrev;
    }

    public Long getNumnota() {
        return numnota;
    }

    public void setNumnota(Long numnota) {
        this.numnota = numnota;
    }

    public String getEntroucaminhao() {
        return entroucaminhao;
    }

    public void setEntroucaminhao(String entroucaminhao) {
        this.entroucaminhao = entroucaminhao;
    }


}
