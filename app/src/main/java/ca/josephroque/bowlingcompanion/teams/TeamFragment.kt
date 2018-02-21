package ca.josephroque.bowlingcompanion.teams

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.josephroque.bowlingcompanion.R
import ca.josephroque.bowlingcompanion.bowlers.Bowler
import ca.josephroque.bowlingcompanion.common.INameAverage
import ca.josephroque.bowlingcompanion.common.NameAverageRecyclerViewAdapter
import ca.josephroque.bowlingcompanion.dummy.DummyContent

/**
 * Copyright (C) 2018 Joseph Roque
 *
 * A fragment representing a list of Teams.
 */
class TeamFragment : Fragment(), NameAverageRecyclerViewAdapter.OnNameAverageInteractionListener {

    companion object {

        /** Logging identifier. */
        private val TAG = "TeamFragment"

        /**
         * Creates a new instance.
         *
         * @return the new instance
         */
        fun newInstance(): TeamFragment {
            return TeamFragment()
        }
    }

    /** Handle team interaction events. */
    private var mListener: OnTeamFragmentInteractionListener? = null

    /** List of teams available. */
    private var mTeams: List<Bowler> = ArrayList()

    /** @Override */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /** @Override */
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_team_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = NameAverageRecyclerViewAdapter(DummyContent.TEAMS, this)
            NameAverageRecyclerViewAdapter.applyDefaultDivider(view, context)
        }
        return view
    }

    /** @Override */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnTeamFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnTeamFragmentInteractionListener")
        }
    }

    /** @Override */
    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /** @Override */
    override fun onNAItemClick(item: INameAverage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /** @Override */
    override fun onNAItemDelete(item: INameAverage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /** @Override */
    override fun onNAItemLongClick(item: INameAverage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Handles interactions with the Team list.
     */
    interface OnTeamFragmentInteractionListener {

        /**
         * Indicates a bowler has been selected and further details should be shown to the user.
         *
         * @param bowler the bowler that the user has selected
         */
        fun onTeamSelected(team: Team)
    }
}