SELECT 
	proc_referencia.nr_processo as nr_processo_referencia,
	proc.nr_processo,
	proc.id_processo,
	classe.cd_classe_judicial,
	classe.ds_classe_judicial
FROM tb_processo proc
INNER JOIN tb_processo_trf ptrf ON (proc.id_processo = ptrf.id_processo_trf)
INNER JOIN tb_processo proc_referencia ON (proc_referencia.id_processo = ptrf.id_proc_referencia)
LEFT JOIN tb_classe_judicial classe ON (classe.id_classe_judicial = ptrf.id_classe_judicial)
WHERE proc_referencia.nr_processo = ANY(:numeros_processos)
ORDER BY ptrf.id_processo_trf