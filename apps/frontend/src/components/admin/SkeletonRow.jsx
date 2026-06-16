/* CSS: classes are defined in pages/admin/ManageUsers.css */

export function SkeletonRow() {
  return (
    <tr className="mu-skel-row" aria-hidden="true">
      <td><div className="mu-skel mu-skel--xs" /></td>
      <td><div className="mu-skel-user"><div className="mu-skel mu-skel--avatar" /><div><div className="mu-skel mu-skel--name" /><div className="mu-skel mu-skel--email" /></div></div></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td><div className="mu-skel mu-skel--sm" /></td>
      <td><div className="mu-skel mu-skel--md" /></td>
      <td><div className="mu-skel mu-skel--pill" /></td>
      <td>
        <div className="mu-skel-acts">
          <div className="mu-skel mu-skel--act" />
          <div className="mu-skel mu-skel--act" />
          <div className="mu-skel mu-skel--act" />
        </div>
      </td>
    </tr>
  );
}
