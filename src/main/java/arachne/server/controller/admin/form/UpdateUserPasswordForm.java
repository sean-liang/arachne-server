package arachne.server.controller.admin.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPasswordForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    private String oldPassword;

    @NotNull
    private String newPassword;

}
