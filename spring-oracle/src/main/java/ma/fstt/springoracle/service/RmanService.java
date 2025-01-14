package ma.fstt.springoracle.service;

import ma.fstt.springoracle.model.BackupHistory;

import java.util.List;

public interface RmanService {

    public List<BackupHistory> listBackups();

    public String performRestore() ;

    public String performIncrementalBackup(int level) ;

    public String performFullBackup();

}
