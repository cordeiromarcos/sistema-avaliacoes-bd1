package br.uel.avaliacoes.model;

public class Usuario {
    private Integer idUsuario;
    private String nome;
    private String email;
    private String matricula;
    private String papel;

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getPapel() { return papel; }
    public void setPapel(String papel) { this.papel = papel; }
}
