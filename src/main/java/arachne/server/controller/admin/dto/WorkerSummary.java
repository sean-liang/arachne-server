package arachne.server.controller.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private List<String> tags;

}
