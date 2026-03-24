package godclassinspector.services;

import java.util.List;

import godclassinspector.model.SourceFileDTO;

public interface AnalysisService {
	void checkGodClass(List<SourceFileDTO> files) throws Exception;
}
