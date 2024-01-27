/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serializable;
import java.util.List;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "USUARIOCAMPOS", uniqueConstraints = {@UniqueConstraint(name = "USUARIO_CAMPOS_NOMBRE", columnNames = {"Nombre"})})
@NamedQueries({
        @NamedQuery(name = "Usuariocampos.findLastId", query = "SELECT MAX(u.id) FROM Usuariocampos u")
})
public class Usuariocampos implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "Id", insertable = false)
    @GenericGenerator(name = "sequence_dep_id", strategy = "service.cloud.request.clientRequest.utils.UsuarioCamposIdGenerator")
    @GeneratedValue(generator = "sequence_dep_id")
    private Integer id;

    @Column(name = "Nombre", unique = true)
    private String nombre;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuariocampos")
    private List<TransaccionUsucampos> transaccionUsucamposList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuariocampos")
    private List<TransaccionContractdocref> transaccionContractdocrefs;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuariocampos")
    private List<TransaccionLineasUsucampos> transaccionLineasUsucamposList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "usuariocampos")
    private List<TransaccionComprobantepagoUsuario> transaccionComprobantepagoUsuarioList;


    @Override
    public String toString() {
        return "org.ventura.cpe.core.dto.hb.Usuariocampos[ id=" + id + " ]";
    }
}
