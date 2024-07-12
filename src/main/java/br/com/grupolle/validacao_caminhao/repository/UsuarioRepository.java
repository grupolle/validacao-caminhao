package br.com.grupolle.validacao_caminhao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import br.com.grupolle.validacao_caminhao.model.Usuarios;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuarios, Long> {

    @Query("SELECT u FROM Usuarios u WHERE u.login = :login")
    Usuarios findByLogin(@Param("login") String login);

    @Query("SELECT u FROM Usuarios u WHERE u.email = :email")
    Usuarios findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM Usuarios u WHERE u.token = :token")
    Usuarios findByToken(@Param("token") String token);
    
    @Query("SELECT u FROM Usuarios u WHERE u.id = :id")
    Usuarios findByid(@Param("id") Long id);
    
    @Query("SELECT u FROM Usuarios u WHERE u.codemp = :codemp")
    Usuarios findByempresa(@Param("codemp") Long codemp);
        
    @Transactional
    @Modifying
    @Query("UPDATE Usuarios u SET u.token = :token WHERE u.login = :login")
    void atualizaTokenUser(@Param("token") String token, @Param("login") String login);
}
