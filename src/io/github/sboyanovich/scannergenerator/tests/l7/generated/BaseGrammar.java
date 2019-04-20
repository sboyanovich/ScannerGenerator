package io.github.sboyanovich.scannergenerator.tests.l7.generated;

import io.github.sboyanovich.scannergenerator.scanner.token.Domain;
import io.github.sboyanovich.scannergenerator.scanner.token.DomainEOP;
import io.github.sboyanovich.scannergenerator.tests.l7.aux.UnifiedAlphabetSymbol;
import java.util.*;
public class BaseGrammar {
	private static List<String> terminalNames;
	private static List<String> nonTerminalNames;
	static {
		terminalNames = new ArrayList<>();
		nonTerminalNames = new ArrayList<>();
		terminalNames.add("\\=");
		terminalNames.add("\\.");
		terminalNames.add("\\|");
		terminalNames.add("TERMINAL");
		terminalNames.add("NON_TERMINAL");
		terminalNames.add("AXM_DECL");
		nonTerminalNames.add("lang");
		nonTerminalNames.add("rule");
		nonTerminalNames.add("rule_list");
		nonTerminalNames.add("lhs");
		nonTerminalNames.add("rhs_list");
		nonTerminalNames.add("rhs");
		nonTerminalNames.add("rhs_list_c");
		nonTerminalNames.add("t");
	}
	public static Map<Integer, Domain> getTermInterpretation(Map<String, Domain> domainNames) {
		Map<Integer, Domain> result = new HashMap<>();
		for(int i = 0; i < terminalNames.size(); i++) {
			result.put(i, domainNames.get(terminalNames.get(i)));
		}
		result.put(terminalNames.size(), DomainEOP.END_OF_PROGRAM);
		return result;
	}
	public static List<String> getNonTerminalNames() {
		return Collections.unmodifiableList(nonTerminalNames);
	}
	public static int getAxiom() {
		return 0;
	}
	public static int[][] getPredictionTable() {
		int[][] result = new int[8][7];
		result[0][0] = -1;
		result[0][1] = -1;
		result[0][2] = -1;
		result[0][3] = -1;
		result[0][4] = 0;
		result[0][5] = 0;
		result[0][6] = -1;
		result[1][0] = -1;
		result[1][1] = -1;
		result[1][2] = -1;
		result[1][3] = -1;
		result[1][4] = 0;
		result[1][5] = 0;
		result[1][6] = -1;
		result[2][0] = -1;
		result[2][1] = -1;
		result[2][2] = -1;
		result[2][3] = -1;
		result[2][4] = 0;
		result[2][5] = 0;
		result[2][6] = 1;
		result[3][0] = -1;
		result[3][1] = -1;
		result[3][2] = -1;
		result[3][3] = -1;
		result[3][4] = 0;
		result[3][5] = 1;
		result[3][6] = -1;
		result[4][0] = -1;
		result[4][1] = 0;
		result[4][2] = 0;
		result[4][3] = 0;
		result[4][4] = 0;
		result[4][5] = -1;
		result[4][6] = -1;
		result[5][0] = -1;
		result[5][1] = 1;
		result[5][2] = 1;
		result[5][3] = 0;
		result[5][4] = 0;
		result[5][5] = -1;
		result[5][6] = -1;
		result[6][0] = -1;
		result[6][1] = 1;
		result[6][2] = 0;
		result[6][3] = -1;
		result[6][4] = -1;
		result[6][5] = -1;
		result[6][6] = -1;
		result[7][0] = -1;
		result[7][1] = -1;
		result[7][2] = -1;
		result[7][3] = 0;
		result[7][4] = 1;
		result[7][5] = -1;
		result[7][6] = -1;
		return result;
	}
	public static Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> getRules() {
		Map<Integer, Map<Integer, List<UnifiedAlphabetSymbol>>> result = new HashMap<>();
		HashMap<Integer, List<UnifiedAlphabetSymbol>> productions = new HashMap<>();
		List<UnifiedAlphabetSymbol> production = new ArrayList<>();
		production.add(new UnifiedAlphabetSymbol(1, false));
		production.add(new UnifiedAlphabetSymbol(2, false));
		productions.put(0, production);
		production = new ArrayList<>();
		result.put(0, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(3, false));
		production.add(new UnifiedAlphabetSymbol(0, true));
		production.add(new UnifiedAlphabetSymbol(4, false));
		production.add(new UnifiedAlphabetSymbol(1, true));
		productions.put(0, production);
		production = new ArrayList<>();
		result.put(1, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(1, false));
		production.add(new UnifiedAlphabetSymbol(2, false));
		productions.put(0, production);
		production = new ArrayList<>();
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(2, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(4, true));
		productions.put(0, production);
		production = new ArrayList<>();
		production.add(new UnifiedAlphabetSymbol(5, true));
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(3, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(5, false));
		production.add(new UnifiedAlphabetSymbol(6, false));
		productions.put(0, production);
		production = new ArrayList<>();
		result.put(4, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(7, false));
		production.add(new UnifiedAlphabetSymbol(5, false));
		productions.put(0, production);
		production = new ArrayList<>();
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(5, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(2, true));
		production.add(new UnifiedAlphabetSymbol(4, false));
		productions.put(0, production);
		production = new ArrayList<>();
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(6, productions);
		productions = new HashMap<>();
		production.add(new UnifiedAlphabetSymbol(3, true));
		productions.put(0, production);
		production = new ArrayList<>();
		production.add(new UnifiedAlphabetSymbol(4, true));
		productions.put(1, production);
		production = new ArrayList<>();
		result.put(7, productions);
		productions = new HashMap<>();
		return result;
	}
}
