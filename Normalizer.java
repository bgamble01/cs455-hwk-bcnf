import java.util.HashSet;
import java.util.Set;

/**
 * This class provides static methods for performing normalization
 * 
 * @author <Ben Gamble>
 * @version <11/24/22>
 */
public class Normalizer {

  /**
   * Performs BCNF decomposition
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of relations (as attribute sets) that are in BCNF
   */
  public static Set<Set<String>> BCNFDecompose(Set<String> rel, FDSet fdset) {
    //  First test if the given relation is already in BCNF with respect to
    // the provided FD set.
     FDSet copied = new FDSet(fdset);
     Set<String> relcopied = new HashSet<>(rel);
     Set<Set<String>> origsuperkeys = findSuperkeys(relcopied, copied);
     Set<Set<String>> output = new HashSet<>();
     
     //System.out.println("BCNF START");

     if(isBCNF(relcopied,copied)) {
        output.add(relcopied);
//        System.out.println("Current Schema= "+ relcopied.toString());
//        System.out.println("Current Schema's Superkeys= "+origsuperkeys.toString());
//        System.out.println("BCNF END");
        return output;
     }
     System.out.println("Current schema = " + relcopied);
     System.out.println("  Current schema's superkeys = " + origsuperkeys);

    //- Identify a nontrivial FD that violates BCNF. Split the relation's
    // attributes using that FD, as seen in class.
     for(FD violater : copied){
        if(!violater.isTrivial() && !origsuperkeys.contains(violater.getLeft())){
           System.out.println("----Splitting on " + violater + " ----");
    //Redistribute the FDs in the closure of fdset to the two new
    // relations (R_Left and R_Right) as follows:
           Set<String> left = new HashSet<>(violater.getLeft());
           left.addAll(violater.getRight());
           Set<String> right = new HashSet<>(rel);
           right.removeAll(violater.getRight());
           right.addAll(violater.getLeft());
           FDSet leftFD = new FDSet();
           FDSet rightFD = new FDSet();
           FDSet closure = new FDSet(FDUtil.fdSetClosure(new FDSet(fdset)));
     //Iterate through closure of the given set of FDs, then union all attributes
     // appearing in the FD, and test if the union is a subset of the R_Left (or
     // R_Right) relation. If so, then the FD gets added to the R_Left's (or R_Right's) FD
     // set. If the union is not a subset of either new relation, then the FD is
     // discarded
           for (FD fd: closure) {
              Set<String> atrs = new HashSet<>(fd.getLeft());
              atrs.addAll(fd.getRight());
              if (left.containsAll(atrs)) {
                 leftFD.add(fd);
              }else if (right.containsAll(atrs)) {
                 rightFD.add(fd);
              }
           }
           System.out.println("Left Schema: "+ left.toString());
           System.out.println("  Left Schema's superkeys = " + findSuperkeys(left, leftFD));
           System.out.println("Right Schema: "+ right.toString());
           System.out.println("  Right Schema's superkeys = " + findSuperkeys(right, rightFD));
           
           Set<Set<String>> schema = new HashSet<Set<String>>();
           schema.addAll(BCNFDecompose(left, leftFD));
           schema.addAll(BCNFDecompose(right, rightFD));
           System.out.println("BCNF End");
          return schema;
        }
     }
     return null;
  }

    // Repeat the above until all relations are in BCNF
 

  /**
   * Tests whether the given relation is in BCNF. A relation is in BCNF iff the
   * left-hand attribute set of all nontrivial FDs is a super key.
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return true if the relation is in BCNF with respect to the specified FD set
   */
  public static boolean isBCNF(Set<String> rel, FDSet fdset) {
    FDSet copied = new FDSet(fdset);
    Set<Set<String>> superkeys = new HashSet<Set<String>>();
    superkeys = Normalizer.findSuperkeys(rel,fdset);
    for (FD individual: copied) {
       if(individual.isTrivial()) {
       }
       else {
          if(!superkeys.contains(individual.getLeft())) {
             return false;
          }
       }
    
    }
    return true;
  }

  /**
   * This method returns a set of super keys
   * 
   * @param rel   A relation (as an attribute set)
   * @param fdset A functional dependency set
   * @return a set of super keys
   */
  public static Set<Set<String>> findSuperkeys(Set<String> rel, FDSet fdset) {
    // sanity check: are all the attributes in the FD set even in the
    // relation? Throw an IllegalArgumentException if not.
     FDSet copied = new FDSet(fdset);
        for (FD individual: copied) {
           if(!rel.containsAll(individual.getLeft())) {
              throw new IllegalArgumentException("Attrribute not in relation");
           }
           if(!rel.containsAll(individual.getRight())) {
              throw new IllegalArgumentException("Attrribute not in relation");
           }
        }
        
    // - iterate through each subset of the relation's attributes, and test
    // the attribute closure of each subset
        Set<Set<String>> subsets = new HashSet<Set<String>>(FDUtil.powerSet(rel));
        Set<Set<String>> superkeys = new HashSet<Set<String>>();
        
        for (Set<String> subset: subsets) {
              Set<String> sc = new HashSet<>(subset);
              boolean changed = true;
              while (changed) {
                 int origAtrs = sc.size();
                 for (FD fd: fdset) {
                    if (sc.containsAll(fd.getLeft())) {
                       sc.addAll(fd.getRight());
                    }
                 }
                 if (sc.size() == origAtrs) {
                    changed = false;
                 }
              }
              if(sc.equals(rel)) { 
              superkeys.add(subset);
           }
        }
    return superkeys;
  }

}
