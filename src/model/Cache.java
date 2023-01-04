package model;

import java.util.ArrayList;

import model.scripting.ArrayVar;
import model.scripting.CoordVar;
import model.scripting.NumVar;
import model.scripting.StringVar;
import model.scripting.Variable;

public class Cache {
	
	public static ArrayList<Cache> caches = new ArrayList<>();
	
	public ArrayList<Variable> variables = new ArrayList<>();
	
	public int index = 0;
	
	public Cache(int i, ArrayList<Variable> vars) {
		index = i;
		for (Variable v : vars) {
			variables.add(copyVariable(v));
		}
	}
	
	public static Variable copyVariable(Variable v) {
		Variable nv = null;
		if (v instanceof NumVar) {
			nv = new NumVar(v.getKey(), (double)v.getValue());
		} else if (v instanceof StringVar) {
			nv = new StringVar(v.getKey(), v.getValue().toString());
		} else if (v instanceof ArrayVar) {
			nv = ArrayVar.copy((ArrayVar) v);
		} else if (v instanceof CoordVar) {
			nv = new CoordVar(v.getKey(), (double[])v.getValue());
		}
		return nv;
	}

	public static ArrayList<Variable> findCodeIndex(int codeIndex) {
		for(int i = 0; i < caches.size(); i++) {
			if (caches.get(i).index == codeIndex) {
				return caches.get(i).variables;
			}
		}
		return null;
	}

}
