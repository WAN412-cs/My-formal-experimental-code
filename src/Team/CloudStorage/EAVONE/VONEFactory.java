package Team.CloudStorage.EAVONE;

public class VONEFactory {
	public VNE GetVONEMethod(int voneMethod) throws Exception
	{
		if(voneMethod == Parameters.MapVONEByDeepReinforceLearning) {
			return new VONEByDeepReinforceLearning();
		} else if(voneMethod == Parameters.MapVONETranModel || voneMethod == Parameters.MapVONEEnTranModel||voneMethod == Parameters.MapVONETranILPByChenxh){
			return new VONEByTranModel();
		} else if(voneMethod == Parameters.MapVONE3ByWangY || voneMethod == Parameters.MapVONEMIPTranAndPRankByCXH || voneMethod == Parameters.MapVONETranILPByChenxh){//k���·��
			return new VONEByKSPath();
		} else if(voneMethod == Parameters.MapVONE3PByWangYAndChenxh){
			return new VONEByEKSPath();
		} else if(voneMethod == Parameters.MapVONECXHNode || voneMethod == Parameters.MapVONE01ILPLin_EquilibriumCXH|| voneMethod == Parameters.MapVONE01ILPLin || voneMethod == Parameters.MapVONE01ILPLinFB || voneMethod == Parameters.MapVONE01ILPLinStrong|| voneMethod == Parameters.MapVONE01ILPPRLinCXH || voneMethod == Parameters.MapVONE01ILPLinCXH || voneMethod == Parameters.MapVONELin_SortByNodeDegree || Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByNodeDegreeAndBW || Parameters.CurrentVONEMethod == Parameters.MapVONELin_SortByBW || voneMethod == Parameters.MapVONELin_FB_SortByNodeDegree || Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByNodeDegreeAndBW || Parameters.CurrentVONEMethod == Parameters.MapVONELin_FB_SortByBW||Parameters.CurrentVONEMethod==Parameters.MapVONE01ILPLin ||Parameters.CurrentVONEMethod==Parameters.MapVONEILPByY_L||Parameters.CurrentVONEMethod==Parameters.MapVONENCRbyILP||Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM||Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM915||Parameters.CurrentVONEMethod==Parameters.MapVONE01ILPNodeRank_Sub||Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM916||Parameters.CurrentVONEMethod==Parameters.MapVONE01ILPLin_EquilibriumCXHnodeilp||Parameters.CurrentVONEMethod==Parameters. MapVONE01ILPLinnodeilp||Parameters.CurrentVONEMethod==Parameters.MapVONENCRbyILPnodeilp||Parameters.CurrentVONEMethod==Parameters.MapVONEILPAM9166){
			return new VONEByLin();//
		} else if(voneMethod == Parameters.MapVONEPageRank||voneMethod == Parameters.MapVONEPageRank_equilibrium||voneMethod == Parameters.MapVONEEnergyByPageRank||voneMethod == Parameters.MapVONEEnergyByPageRankEasy||voneMethod == Parameters.MapVONEEnergyByVogelPageRank||voneMethod == Parameters.MapVONEPageRankOfGHG
				||voneMethod == Parameters.MapVONEPageRankOfGHGByVogel ||voneMethod==Parameters.MapVONEPageRankOfGHGByVogelPro||voneMethod==Parameters.TestMethod||voneMethod==Parameters.MapVONEAM||voneMethod==Parameters.MapVONEAM913||voneMethod == Parameters.MapVONE_ESE||voneMethod == Parameters.MapEVONENodeRank
				||voneMethod == Parameters.MapVONENodeRankZM||voneMethod == Parameters.MapVONENodeRank_SubNet
				||voneMethod == Parameters.MapVONEPageRankOfGHGByVogelcenm||voneMethod == Parameters.MapVONEDRLMD_VONE){
			return new VONEByPageRank();
		} else {
			throw new Exception("no such " + voneMethod+" found.");
		}
	}
}
